package com.rhetorica.app.data.seed

import android.content.Context
import androidx.room.withTransaction
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.DictionaryEntity
import com.rhetorica.app.data.local.IdiomDao
import com.rhetorica.app.data.local.IdiomEntity
import com.rhetorica.app.data.local.QuoteDao
import com.rhetorica.app.data.local.QuoteEntity
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.SpeechDao
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.local.WordProgressDao
import com.rhetorica.app.core.util.AppLog
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
    private val wordProgressDao: WordProgressDao,
    private val idiomDao: IdiomDao,
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
            AppLog.i(TAG, "Seed data up to date (v$SEED_VERSION, $wordCount words). Skipping reload.")
            return@withContext
        }

        AppLog.i(TAG, "Loading seed data (applied=$appliedVersion, target=$SEED_VERSION, words=$wordCount)")
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
                val idioms = loadIdioms(validOratorIds, quotesResult.items)

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
                    AppLog.w(TAG, "Skipping quote prune after partial quote load failure")
                }
                if (speechesResult.loadedCleanly) {
                    pruneByIds(
                        existingIds = speechDao.getAllSpeechIds(),
                        keepIds = speechesResult.items.map { it.id }.toSet(),
                        deleteChunk = speechDao::deleteSpeechesByIds,
                    )
                } else {
                    AppLog.w(TAG, "Skipping speech prune after speech load failure")
                }
                pruneByIds(
                    existingIds = idiomDao.getAllIds(),
                    keepIds = idioms.map { it.id }.toSet(),
                    deleteChunk = idiomDao::deleteByIds,
                )
                savedWordDao.deleteOrphanedSavedWords()
                wordProgressDao.deleteOrphaned()
            }
            prefs.edit().putInt(KEY_SEED_VERSION, SEED_VERSION).apply()
            AppLog.i(
                TAG,
                "Seed data load complete (v$SEED_VERSION, words=${wordDao.wordCount()}, idioms=${idiomDao.idiomCount()})",
            )
        } catch (e: Exception) {
            AppLog.e(TAG, "Seed data load failed; leaving seed_version=$appliedVersion", e)
        }
    }

    companion object {
        /** Increment when bundled seed assets change. */
        const val SEED_VERSION = 25
        private const val PREFS_NAME = "rhetorica_seed"
        private const val KEY_SEED_VERSION = "seed_version"
        private const val MIN_EXPECTED_WORDS = 50
        private const val MIN_EXPECTED_IDIOMS = 100
        private val IDIOM_COMPLEXITIES = setOf("beginner", "basic", "intermediate", "advanced")
        /** Stay under typical SQLite max variable count (~999). */
        private const val DELETE_CHUNK_SIZE = 400
    }

    private suspend fun loadDictionaries(): List<DictionaryEntity> {
        val dictionaries = try {
            context.assets.open("data/seed/dictionaries.json").bufferedReader().use { reader ->
                json.decodeFromString<List<DictionaryEntity>>(reader.readText())
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to load dictionaries", e)
            throw SeedLoadException("Failed to load dictionaries.json", e)
        }
        if (dictionaries.isEmpty()) {
            throw SeedLoadException("dictionaries.json decoded to an empty list")
        }
        dictionaryDao.upsertDictionaries(dictionaries)
        AppLog.i(TAG, "Loaded ${dictionaries.size} dictionaries")
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
                AppLog.e(TAG, "Failed to load words from $fileName", e)
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
                AppLog.w(TAG, "Word file is empty: $fileName")
            }

            allWords.addAll(words)
            AppLog.i(TAG, "Loaded ${words.size} words from $fileName")
        }

        if (allWords.isEmpty()) {
            throw SeedLoadException("No valid words loaded from seed assets")
        }

        wordDao.upsertWords(allWords)
        AppLog.i(TAG, "Loaded ${allWords.size} total words")
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
                        AppLog.w(TAG, "Skipping quote with invalid oratorId: ${quote.oratorId}")
                    }
                    isValid
                }
                allQuotes.addAll(validQuotes)
                AppLog.i(TAG, "Loaded ${validQuotes.size} quotes from $fileName")
            } catch (e: Exception) {
                loadedCleanly = false
                AppLog.e(TAG, "Failed to load quotes from $fileName", e)
            }
        }

        if (allQuotes.isNotEmpty()) {
            quoteDao.upsertQuotes(allQuotes)
            AppLog.i(TAG, "Loaded ${allQuotes.size} total quotes")
        }
        return AssetLoadResult(items = allQuotes, loadedCleanly = loadedCleanly)
    }

    private suspend fun loadSpeeches(validOratorIds: Set<Long>): AssetLoadResult<SpeechEntity> {
        val speeches = try {
            context.assets.open("data/seed/speeches.json").bufferedReader().use { reader ->
                json.decodeFromString<List<SpeechEntity>>(reader.readText())
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to load speeches", e)
            return AssetLoadResult(items = emptyList(), loadedCleanly = false)
        }

        val validSpeeches = speeches.filter { speech ->
            val isValid = speech.oratorId in validOratorIds
            if (!isValid) {
                AppLog.w(TAG, "Skipping speech with invalid oratorId: ${speech.oratorId}")
            }
            isValid
        }
        if (validSpeeches.isNotEmpty()) {
            speechDao.upsertSpeeches(validSpeeches)
            AppLog.i(TAG, "Loaded ${validSpeeches.size} speeches from speeches.json")
        }
        return AssetLoadResult(items = validSpeeches, loadedCleanly = true)
    }

    /**
     * Idioms are one asset. A parse failure, a thin file, or a quote link that does not
     * match a loaded quote aborts the seed load so the version is not bumped.
     */
    private suspend fun loadIdioms(
        validOratorIds: Set<Long>,
        quotes: List<QuoteEntity>,
    ): List<IdiomEntity> {
        val idioms = try {
            context.assets.open("data/seed/idioms.json").bufferedReader().use { reader ->
                json.decodeFromString<List<IdiomEntity>>(reader.readText())
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to load idioms", e)
            throw SeedLoadException("Failed to load idioms.json", e)
        }
        if (idioms.size < MIN_EXPECTED_IDIOMS) {
            throw SeedLoadException("Idiom load produced only ${idioms.size} rows (min $MIN_EXPECTED_IDIOMS)")
        }
        val duplicateIds = idioms.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
        if (duplicateIds.isNotEmpty()) {
            throw SeedLoadException("Duplicate idiom ids: $duplicateIds")
        }
        val quotesById = quotes.associateBy { it.id }
        val invalid = idioms.filter { idiom ->
            idiom.idiom.isBlank() ||
                idiom.meaning.isBlank() ||
                idiom.example.isBlank() ||
                idiom.complexity !in IDIOM_COMPLEXITIES ||
                (idiom.oratorId != null && idiom.oratorId !in validOratorIds) ||
                (idiom.quoteId != null && !quoteLinkIsValid(idiom, quotesById))
        }
        if (invalid.isNotEmpty()) {
            val sample = invalid.take(3).joinToString { idiom -> idiom.idiom }
            throw SeedLoadException("idioms.json has ${invalid.size} invalid row(s) (e.g. $sample)")
        }
        idiomDao.upsertIdioms(idioms)
        AppLog.i(TAG, "Loaded ${idioms.size} idioms")
        return idioms
    }

    private fun quoteLinkIsValid(idiom: IdiomEntity, quotesById: Map<Long, QuoteEntity>): Boolean {
        val oratorId = idiom.oratorId ?: return false
        val quote = idiom.quoteId?.let { quotesById[it] } ?: return false
        return quote.oratorId == oratorId
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
        AppLog.i(TAG, "Pruned ${toDelete.size} stale rows")
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
