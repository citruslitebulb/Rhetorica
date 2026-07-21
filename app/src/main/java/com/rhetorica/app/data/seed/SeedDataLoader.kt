package com.rhetorica.app.data.seed

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.DictionaryEntity
import com.rhetorica.app.data.local.QuoteDao
import com.rhetorica.app.data.local.QuoteEntity
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.SpeechDao
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.local.WordEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class SeedDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordDao: WordDao,
    private val dictionaryDao: DictionaryDao,
    private val quoteDao: QuoteDao,
    private val speechDao: SpeechDao,
    private val savedWordDao: SavedWordDao,
    private val database: RhetoricaDatabase,
    private val json: Json,
) {
    private val TAG = "SeedDataLoader"

    /**
     * Bump when seed JSON content changes so returning installs re-import assets.
     * Skips the full upsert path when the stored version already matches.
     *
     * On success: upserts seed rows, prunes IDs no longer in assets, and advances
     * [SEED_VERSION]. On failure: does not advance the version so the next launch retries.
     *
     * Safety rules:
     * - Any `words_*.json` failure aborts the whole load (no prune / no version bump).
     * - Quotes/speeches prune only when their assets loaded without parse errors.
     * - Deletes are chunked to stay under SQLite bind-variable limits.
     */
    suspend fun loadSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val appliedVersion = prefs.getInt(KEY_SEED_VERSION, 0)
        val wordCount = wordDao.wordCount()

        if (appliedVersion == SEED_VERSION && wordCount > 0) {
            Log.i(TAG, "Seed data up to date (v$SEED_VERSION, $wordCount words). Skipping reload.")
            return@withContext
        }

        Log.i(TAG, "Loading seed data (applied=$appliedVersion, target=$SEED_VERSION, words=$wordCount)")
        try {
            database.withTransaction {
                val dictionaries = loadDictionaries()
                val validOratorIds = dictionaries.map { it.id }.toSet()

                val words = loadWords(validOratorIds)
                if (words.size < MIN_EXPECTED_WORDS) {
                    throw SeedLoadException(
                        "Word load produced only ${words.size} rows (min $MIN_EXPECTED_WORDS)",
                    )
                }

                val quotesResult = loadQuotes(validOratorIds)
                val speechesResult = loadSpeeches(validOratorIds)

                // Prune only after a complete, successful asset read for that table.
                pruneByIds(
                    existingIds = dictionaryDao.getAllDictionaryIds(),
                    keepIds = dictionaries.map { it.id }.toSet(),
                    deleteChunk = dictionaryDao::deleteDictionariesByIds,
                )
                pruneByIds(
                    existingIds = wordDao.getAllWordIds(),
                    keepIds = words.map { it.id }.toSet(),
                    deleteChunk = wordDao::deleteWordsByIds,
                )
                if (quotesResult.loadedCleanly) {
                    pruneByIds(
                        existingIds = quoteDao.getAllQuoteIds(),
                        keepIds = quotesResult.items.map { it.id }.toSet(),
                        deleteChunk = quoteDao::deleteQuotesByIds,
                    )
                } else {
                    Log.w(TAG, "Skipping quote prune after partial quote load failure")
                }
                if (speechesResult.loadedCleanly) {
                    pruneByIds(
                        existingIds = speechDao.getAllSpeechIds(),
                        keepIds = speechesResult.items.map { it.id }.toSet(),
                        deleteChunk = speechDao::deleteSpeechesByIds,
                    )
                } else {
                    Log.w(TAG, "Skipping speech prune after speech load failure")
                }
                savedWordDao.deleteOrphanedSavedWords()
            }
            prefs.edit().putInt(KEY_SEED_VERSION, SEED_VERSION).apply()
            Log.i(
                TAG,
                "Seed data load complete (v$SEED_VERSION, words=${wordDao.wordCount()})",
            )
        } catch (e: Exception) {
            Log.e(TAG, "Seed data load failed; leaving seed_version=$appliedVersion", e)
        }
    }

    companion object {
        /** Increment when bundled seed assets change. */
        const val SEED_VERSION = 7
        private const val PREFS_NAME = "rhetorica_seed"
        private const val KEY_SEED_VERSION = "seed_version"
        private const val MIN_EXPECTED_WORDS = 50
        /** Stay under typical SQLite max variable count (~999). */
        private const val DELETE_CHUNK_SIZE = 400
    }

    private suspend fun loadDictionaries(): List<DictionaryEntity> {
        val dictionaries = try {
            context.assets.open("data/seed/dictionaries.json").bufferedReader().use { reader ->
                json.decodeFromString<List<DictionaryEntity>>(reader.readText())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load dictionaries", e)
            throw SeedLoadException("Failed to load dictionaries.json", e)
        }
        if (dictionaries.isEmpty()) {
            throw SeedLoadException("dictionaries.json decoded to an empty list")
        }
        dictionaryDao.upsertDictionaries(dictionaries)
        Log.i(TAG, "Loaded ${dictionaries.size} dictionaries")
        return dictionaries
    }

    /**
     * Loads all word files. **Any** file failure aborts so we never prune against a partial keep-set.
     */
    private suspend fun loadWords(validOratorIds: Set<Long>): List<WordEntity> {
        val allWords = mutableListOf<WordEntity>()
        val seedDir = "data/seed"
        val oratorFiles = context.assets.list(seedDir)
            ?.filter { it.startsWith("words_") && it.endsWith(".json") }
            ?.sorted()
            ?.map { "$seedDir/$it" }
            ?: emptyList()

        if (oratorFiles.isEmpty()) {
            throw SeedLoadException("No words_*.json files found under $seedDir")
        }

        oratorFiles.forEach { fileName ->
            val words = try {
                context.assets.open(fileName).bufferedReader().use { reader ->
                    json.decodeFromString<List<WordEntity>>(reader.readText())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load words from $fileName", e)
                throw SeedLoadException("Failed to load $fileName", e)
            }

            val invalidOratorIds = words
                .mapNotNull { word ->
                    val id = word.oratorId
                    if (id == null || id !in validOratorIds) {
                        id to word.word
                    } else {
                        null
                    }
                }
            if (invalidOratorIds.isNotEmpty()) {
                val sample = invalidOratorIds.take(3).joinToString { (id, w) -> "$w(oratorId=$id)" }
                throw SeedLoadException(
                    "$fileName has ${invalidOratorIds.size} word(s) with invalid oratorId " +
                        "(e.g. $sample). Aborting so we never prune against a partial keep-set.",
                )
            }
            if (words.isEmpty()) {
                Log.w(TAG, "Word file is empty: $fileName")
            }

            allWords.addAll(words)
            Log.i(TAG, "Loaded ${words.size} words from $fileName")
        }

        if (allWords.isEmpty()) {
            throw SeedLoadException("No valid words loaded from seed assets")
        }

        wordDao.upsertWords(allWords)
        Log.i(TAG, "Loaded ${allWords.size} total words")
        return allWords
    }

    private suspend fun loadQuotes(validOratorIds: Set<Long>): AssetLoadResult<QuoteEntity> {
        val allQuotes = mutableListOf<QuoteEntity>()
        val seedDir = "data/seed"
        val oratorQuoteFiles = context.assets.list(seedDir)
            ?.filter { it.startsWith("quotes_") && it.endsWith(".json") }
            ?.sorted()
            ?.map { "$seedDir/$it" }
            ?: emptyList()

        var loadedCleanly = true
        oratorQuoteFiles.forEach { fileName ->
            try {
                val quotes = context.assets.open(fileName).bufferedReader().use { reader ->
                    json.decodeFromString<List<QuoteEntity>>(reader.readText())
                }
                val validQuotes = quotes.filter { quote ->
                    val isValid = quote.oratorId in validOratorIds
                    if (!isValid) {
                        Log.w(TAG, "Skipping quote with invalid oratorId: ${quote.oratorId}")
                    }
                    isValid
                }
                allQuotes.addAll(validQuotes)
                Log.i(TAG, "Loaded ${validQuotes.size} quotes from $fileName")
            } catch (e: Exception) {
                loadedCleanly = false
                Log.e(TAG, "Failed to load quotes from $fileName", e)
            }
        }

        if (allQuotes.isNotEmpty()) {
            quoteDao.upsertQuotes(allQuotes)
            Log.i(TAG, "Loaded ${allQuotes.size} total quotes")
        }
        return AssetLoadResult(items = allQuotes, loadedCleanly = loadedCleanly)
    }

    private suspend fun loadSpeeches(validOratorIds: Set<Long>): AssetLoadResult<SpeechEntity> {
        val speeches = try {
            context.assets.open("data/seed/speeches.json").bufferedReader().use { reader ->
                json.decodeFromString<List<SpeechEntity>>(reader.readText())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load speeches", e)
            return AssetLoadResult(items = emptyList(), loadedCleanly = false)
        }

        val validSpeeches = speeches.filter { speech ->
            val isValid = speech.oratorId in validOratorIds
            if (!isValid) {
                Log.w(TAG, "Skipping speech with invalid oratorId: ${speech.oratorId}")
            }
            isValid
        }
        if (validSpeeches.isNotEmpty()) {
            speechDao.upsertSpeeches(validSpeeches)
            Log.i(TAG, "Loaded ${validSpeeches.size} speeches from speeches.json")
        }
        return AssetLoadResult(items = validSpeeches, loadedCleanly = true)
    }

    private suspend fun pruneByIds(
        existingIds: List<Long>,
        keepIds: Set<Long>,
        deleteChunk: suspend (List<Long>) -> Unit,
    ) {
        val toDelete = existingIds.filter { it !in keepIds }
        if (toDelete.isEmpty()) return
        toDelete.chunked(DELETE_CHUNK_SIZE).forEach { chunk ->
            deleteChunk(chunk)
        }
        Log.i(TAG, "Pruned ${toDelete.size} stale rows")
    }
}

private data class AssetLoadResult<T>(
    val items: List<T>,
    /** False if any asset failed to parse — do not prune this table. */
    val loadedCleanly: Boolean,
)

private class SeedLoadException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
