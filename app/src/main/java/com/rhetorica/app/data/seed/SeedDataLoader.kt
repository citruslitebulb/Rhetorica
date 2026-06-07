package com.rhetorica.app.data.seed

import android.content.Context
import android.util.Log
import com.rhetorica.app.data.local.DictionaryEntity
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.QuoteEntity
import com.rhetorica.app.data.local.QuoteDao
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.SpeechDao
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.local.WordDao
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordDao: WordDao,
    private val dictionaryDao: DictionaryDao,
    private val quoteDao: QuoteDao,
    private val speechDao: SpeechDao,
    private val database: RhetoricaDatabase,
    private val json: Json,
) {
    private val TAG = "SeedDataLoader"

    suspend fun loadSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        database.withTransaction {
            // Always load dictionaries (upsert) to pick up updates such as new themeCategories on orators
            loadDictionaries()
            
            // Always load words to add any new seed data
            // upsertWords uses OnConflictStrategy.REPLACE to handle existing words
            loadWords()

            // Always load quotes to support adding more over time (100 famous speech excerpts per orator)
            loadQuotes()

            // Load full speeches for deeper reading, linked from word examples
            loadSpeeches()
        }
    }

    private suspend fun getValidOratorIds(): Set<Long> {
        return dictionaryDao.getAllDictionaries()
            .map { it.id }
            .toSet()
    }

    private suspend fun loadDictionaries() {
        val dictionaries = try {
            context.assets.open("data/seed/dictionaries.json").bufferedReader().use { reader ->
                val jsonString = reader.readText()
                json.decodeFromString<List<DictionaryEntity>>(jsonString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load dictionaries", e)
            emptyList()
        }
        if (dictionaries.isEmpty()) {
            Log.e(TAG, "No dictionaries loaded from seed data")
            return
        }
        dictionaryDao.upsertDictionaries(dictionaries)
        Log.i(TAG, "Loaded ${dictionaries.size} dictionaries")
    }

    private suspend fun loadWords() {
        val allWords = mutableListOf<WordEntity>()

        // Dynamically discover all words_*.json files so new orators can be added without code changes
        val seedDir = "data/seed"
        val oratorFiles = context.assets.list(seedDir)
            ?.filter { it.startsWith("words_") && it.endsWith(".json") }
            ?.sorted()
            ?.map { "$seedDir/$it" }
            ?: emptyList()

        val validOratorIds = getValidOratorIds()

        oratorFiles.forEach { fileName ->
            try {
                val words = context.assets.open(fileName).bufferedReader().use { reader ->
                    val jsonString = reader.readText()
                    json.decodeFromString<List<WordEntity>>(jsonString)
                }
                val validWords = words.filter { word ->
                    val isValid = word.oratorId in validOratorIds
                    if (!isValid) {
                        Log.w(TAG, "Skipping word with invalid oratorId: ${word.oratorId}")
                    }
                    isValid
                }
                allWords.addAll(validWords)
                Log.i(TAG, "Loaded ${validWords.size} words from $fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load words from $fileName", e)
            }
        }
        
        if (allWords.isNotEmpty()) {
            wordDao.upsertWords(allWords)
            Log.i(TAG, "Loaded ${allWords.size} total words")
        }
    }

    private suspend fun loadQuotes() {
        val allQuotes = mutableListOf<QuoteEntity>()

        // Dynamically discover all quotes_*.json files so new orators (e.g. modern tech leaders)
        // can be added by just dropping in new JSON files. No more hardcoded lists.
        val seedDir = "data/seed"
        val oratorQuoteFiles = context.assets.list(seedDir)
            ?.filter { it.startsWith("quotes_") && it.endsWith(".json") }
            ?.sorted()
            ?.map { "$seedDir/$it" }
            ?: emptyList()

        val validOratorIds = getValidOratorIds()

        oratorQuoteFiles.forEach { fileName ->
            try {
                val quotes = context.assets.open(fileName).bufferedReader().use { reader ->
                    val jsonString = reader.readText()
                    json.decodeFromString<List<QuoteEntity>>(jsonString)
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
                Log.e(TAG, "Failed to load quotes from $fileName", e)
            }
        }

        if (allQuotes.isNotEmpty()) {
            quoteDao.upsertQuotes(allQuotes)
            Log.i(TAG, "Loaded ${allQuotes.size} total quotes")
        }
    }

    private suspend fun loadSpeeches() {
        val allSpeeches = mutableListOf<SpeechEntity>()

        try {
            val speeches = context.assets.open("data/seed/speeches.json").bufferedReader().use { reader ->
                val jsonString = reader.readText()
                json.decodeFromString<List<SpeechEntity>>(jsonString)
            }
            val validOratorIds = getValidOratorIds()
            val validSpeeches = speeches.filter { speech ->
                val isValid = speech.oratorId in validOratorIds
                if (!isValid) {
                    Log.w(TAG, "Skipping speech with invalid oratorId: ${speech.oratorId}")
                }
                isValid
            }
            allSpeeches.addAll(validSpeeches)
            Log.i(TAG, "Loaded ${validSpeeches.size} speeches from speeches.json")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load speeches", e)
        }

        if (allSpeeches.isNotEmpty()) {
            speechDao.upsertSpeeches(allSpeeches)
            Log.i(TAG, "Loaded ${allSpeeches.size} total speeches")
        }
    }
}
