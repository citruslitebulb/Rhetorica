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

    /**
     * Pick a random library word suitable for letter-guessing.
     * Matches playable letter length (A–Z only) in [minLetters, maxLetters] inclusive.
     *
     * Prefer candidates that are not in [excludeWordIds] and whose definition is not in
     * [excludeDefinitions] so consecutive rounds never reuse the same prompt/answer pair.
     */
    suspend fun getRandomWordForLetterGuess(
        oratorId: Long?,
        minLetters: Int,
        maxLetters: Int,
        excludeWordIds: Set<Long> = emptySet(),
        excludeDefinitions: Set<String> = emptySet(),
        poolSize: Int = 500,
    ): WordEntity? {
        fun letterCountOk(word: WordEntity): Boolean {
            val letters = word.word.filter { it.isLetter() }
            return letters.isNotEmpty() && letters.length in minLetters..maxLetters
        }

        suspend fun pool(orator: Long?): List<WordEntity> {
            return if (orator == null) {
                wordDao.getRandomWordsPool(poolSize)
            } else {
                wordDao.getRandomWordsPoolByOrator(orator, poolSize)
            }.filter(::letterCountOk)
        }

        fun pick(candidates: List<WordEntity>): WordEntity? {
            if (candidates.isEmpty()) return null
            val notSameWord = candidates.filter { it.id !in excludeWordIds }
            val notSameDef = notSameWord.filter {
                it.definition.trim().lowercase() !in excludeDefinitions
            }
            return when {
                notSameDef.isNotEmpty() -> notSameDef.random()
                notSameWord.isNotEmpty() -> notSameWord.random()
                else -> candidates.random()
            }
        }

        pick(pool(oratorId))?.let { return it }

        // Fall back to full library if the selected orator is too thin for this band.
        if (oratorId != null) {
            pick(pool(null))?.let { return it }
        }

        // Last resort: larger pool, still prefer exclusions.
        return pick(wordDao.getRandomWordsPool(poolSize * 3).filter(::letterCountOk))
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
