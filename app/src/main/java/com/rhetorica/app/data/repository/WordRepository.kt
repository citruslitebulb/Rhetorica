package com.rhetorica.app.data.repository

import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.SavedWordEntity
import com.rhetorica.app.data.local.SavedWordSummary
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.local.WordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val savedWordDao: SavedWordDao,
    private val dictionaryDao: DictionaryDao,
) {
    fun observeWords(): Flow<List<WordEntity>> = wordDao.observeWords()

    fun observeWordById(wordId: Long): Flow<WordEntity?> = wordDao.observeWordById(wordId)

    fun observeSavedWordIds(): Flow<List<Long>> = savedWordDao.observeSavedWordIds()

    fun observeSavedWordSummaries(): Flow<List<SavedWordSummary>> = savedWordDao.observeSavedWordSummaries()

    fun observeIsWordSaved(wordId: Long): Flow<Boolean> = savedWordDao.observeIsWordSaved(wordId)

    fun observeWordsByOrator(oratorId: Long?): Flow<List<WordEntity>> {
        return if (oratorId == null) {
            wordDao.observeWords()
        } else {
            wordDao.observeWordsByOrator(oratorId)
        }
    }

    suspend fun getWordOfTheDay(): WordEntity? = getWordOfTheDayForPreferences(
        selectedOratorId = null,
        rotateThroughAll = true,
    )

    suspend fun getWordOfTheDayByOrator(oratorId: Long?): WordEntity? {
        return getWordOfTheDayForPreferences(
            selectedOratorId = oratorId,
            rotateThroughAll = oratorId == null,
        )
    }

    /**
     * Word of the Day for the user's orator preference.
     * When a specific orator is selected (and not rotating), the word is always
     * drawn from that orator's list — never a global word with a swapped label.
     */
    suspend fun getWordOfTheDayForPreferences(
        selectedOratorId: Long?,
        rotateThroughAll: Boolean,
    ): WordEntity? {
        val oratorId = WordOfDaySelector.resolveOratorId(
            selectedOratorId = selectedOratorId,
            rotateThroughAll = rotateThroughAll,
        )
        return if (oratorId == null) {
            val count = wordDao.wordCount()
            if (count == 0) return null
            val offset = WordOfDaySelector.dayOffset(count)
            wordDao.getWordOfTheDay(offset)
        } else {
            val count = wordDao.wordCountByOrator(oratorId)
            if (count == 0) return null
            val offset = WordOfDaySelector.dayOffset(count)
            wordDao.getWordOfTheDayByOrator(oratorId, offset)
        }
    }

    suspend fun getRandomWords(limit: Int): List<WordEntity> = wordDao.getRandomWords(limit)

    suspend fun getRandomWords(
        limit: Int,
        oratorId: Long?,
    ): List<WordEntity> {
        return if (oratorId == null) {
            wordDao.getRandomWords(limit)
        } else {
            wordDao.getRandomWordsByOrator(oratorId, limit)
        }
    }

    suspend fun saveWord(wordId: Long) {
        savedWordDao.saveWord(
            SavedWordEntity(
                wordId = wordId,
                savedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun unsaveWord(wordId: Long) {
        savedWordDao.unsaveWord(wordId)
    }

    suspend fun toggleSaved(wordId: Long) {
        if (savedWordDao.isWordSaved(wordId)) {
            savedWordDao.unsaveWord(wordId)
        } else {
            saveWord(wordId)
        }
    }

    suspend fun seedWordsIfEmpty() {
        // Redundant seeding removed. Seeding is handled by SeedDataLoader in RhetoricaApp.
    }

    suspend fun fixNullOratorIds() {
        val dictionaries = dictionaryDao.getAllDictionaries()
        val defaultOratorId = dictionaries.firstOrNull()?.id ?: 18L

        val wordsWithNullOrator = wordDao.getWordsWithNullOratorId()
        if (wordsWithNullOrator.isNotEmpty()) {
            val updatedWords = wordsWithNullOrator.map { it.copy(oratorId = defaultOratorId) }
            wordDao.upsertWords(updatedWords)
        }
    }
}
