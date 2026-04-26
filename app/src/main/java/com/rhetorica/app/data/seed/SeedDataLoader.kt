package com.rhetorica.app.data.seed

import android.content.Context
import android.util.Log
import com.rhetorica.app.data.local.DictionaryEntity
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.RhetoricaDatabase
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.local.WordDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wordDao: WordDao,
    private val dictionaryDao: DictionaryDao,
    private val database: RhetoricaDatabase,
    private val json: Json,
) {
    private val TAG = "SeedDataLoader"

    suspend fun loadSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        database.runInTransaction {
            val dictionaryCount = dictionaryDao.dictionaryCount()
            if (dictionaryCount == 0) {
                loadDictionaries()
                loadWords()
            }
        }
    }

    private suspend fun getValidOratorIds(): Set<Long> {
        return dictionaryDao.getAllDictionaries()
            .map { it.id }
            .toSet()
    }

    private suspend fun loadDictionaries() {
        val dictionaries = try {
            context.assets.open("data/seed/dictionaries.json").use { inputStream ->
                json.decodeFromStream<List<DictionaryEntity>>(inputStream)
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
        
        val oratorFiles = listOf(
            "data/seed/words_shakespeare.json",
            "data/seed/words_mlk.json",
            "data/seed/words_churchill.json"
        )
        
        val validOratorIds = getValidOratorIds()
        
        oratorFiles.forEach { fileName ->
            try {
                val words = context.assets.open(fileName).use { inputStream ->
                    json.decodeFromStream<List<WordEntity>>(inputStream)
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
}
