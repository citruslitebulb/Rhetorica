package com.rhetorica.app.data.repository

import com.rhetorica.app.core.model.LearningPool
import com.rhetorica.app.core.model.OratorCatalogKind
import com.rhetorica.app.data.local.DictionaryDao
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.SavedWordEntity
import com.rhetorica.app.data.local.SavedWordSummary
import com.rhetorica.app.data.local.SpeechDao
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.local.orDefault
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val savedWordDao: SavedWordDao,
    private val dictionaryDao: DictionaryDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val speechDao: SpeechDao,
    private val preferencesRepository: PreferencesRepository,
) {
    fun observeWordById(wordId: Long): Flow<WordEntity?> = wordDao.observeWordById(wordId)

    fun observeSavedWordIds(): Flow<List<Long>> = savedWordDao.observeSavedWordIds()

    fun observeSavedWordSummaries(): Flow<List<SavedWordSummary>> = savedWordDao.observeSavedWordSummaries()

    fun observeIsWordSaved(wordId: Long): Flow<Boolean> = savedWordDao.observeIsWordSaved(wordId)

    /**
     * Feed query that stays on SQL for orator scoping instead of loading the full library
     * whenever a single orator or a theme-limited set is selected.
     */
    fun observeWordsForFeed(oratorId: Long?, oratorIds: Collection<Long>?): Flow<List<WordEntity>> {
        return when {
            oratorId != null -> wordDao.observeWordsByOrator(oratorId)
            !oratorIds.isNullOrEmpty() -> wordDao.observeWordsByOratorIds(oratorIds.toList())
            oratorIds != null && oratorIds.isEmpty() -> flowOf(emptyList())
            else -> wordDao.observeWords()
        }
    }

    suspend fun searchWords(query: String): List<WordEntity> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val visible = resolveLibraryOratorIds()
        if (visible.isEmpty()) return emptyList()
        return wordDao.searchWordsInOrators(trimmed, visible)
    }

    suspend fun searchSpeeches(query: String): List<SpeechEntity> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()
        val visible = resolveVisibleOratorIds()
        if (visible.isEmpty()) return emptyList()
        return speechDao.searchSpeechesInOrators(trimmed, visible)
    }

    /**
     * Word of the Day for the user's orator preference.
     * Stable for the local calendar day. Skips previously shown words until the pool
     * is exhausted, then starts a new cycle.
     */
    suspend fun getWordOfTheDayForPreferences(
        selectedOratorId: Long?,
        rotateThroughAll: Boolean,
    ): WordEntity? {
        val preferences = userPreferencesDao.getUserPreferences().orDefault()
        val visible = resolveVisibleOratorIds(preferences)
        val oratorId = WordOfDaySelector.resolveOratorId(
            selectedOratorId = selectedOratorId,
            rotateThroughAll = rotateThroughAll,
            visibleOratorIds = visible,
        )
        val favorites = if (oratorId == null) {
            preferences.favoriteOratorIds.filter { it in visible }
        } else {
            emptyList()
        }
        return ensureTodaysWord(
            oratorId = oratorId,
            favoriteOratorIds = favorites,
            visibleOratorIds = visible,
        )
    }

    suspend fun ensureTodaysWord(
        oratorId: Long? = null,
        favoriteOratorIds: List<Long> = emptyList(),
        visibleOratorIds: Collection<Long>? = null,
        today: String = LocalDate.now(ZoneId.systemDefault()).toString(),
    ): WordEntity? {
        val preferences = userPreferencesDao.getUserPreferences().orDefault()
        val visible = visibleOratorIds ?: resolveVisibleOratorIds(preferences)
        val resolvedOratorId = WordOfDaySelector.resolveOratorId(
            selectedOratorId = oratorId ?: preferences.selectedOratorId,
            rotateThroughAll = if (oratorId != null) false else preferences.rotateThroughAll,
            visibleOratorIds = visible,
        )
        val resolvedFavorites = if (resolvedOratorId == null) {
            favoriteOratorIds.ifEmpty { preferences.favoriteOratorIds }.filter { it in visible }
        } else {
            emptyList()
        }
        val key = WordOfDaySelector.poolKey(
            oratorId = resolvedOratorId,
            favoriteOratorIds = resolvedFavorites,
            includeLiterary = preferences.includeLiteraryOrators,
            includeFictional = preferences.includeFictionalOrators,
        )

        if (
            preferences.todaysWotdDate == today &&
            preferences.todaysWotdId != null &&
            preferences.shownWotdPoolKey == key
        ) {
            val existing = wordDao.getWordById(preferences.todaysWotdId)
            if (existing != null) return existing
        }

        val allWords = loadPool(resolvedOratorId, resolvedFavorites, visible)
        val shown = if (preferences.shownWotdPoolKey == key) {
            preferences.shownWotdIds.toSet()
        } else {
            emptySet()
        }
        val pick = WordOfDaySelector.selectUnseen(
            allWords = allWords,
            oratorId = resolvedOratorId,
            shownIds = shown,
            favoriteOratorIds = resolvedFavorites,
            visibleOratorIds = visible,
        )
        val word = pick.word ?: return null
        preferencesRepository.update { current ->
            current.copy(
                shownWotdIds = pick.shownIds,
                shownWotdPoolKey = key,
                todaysWotdId = word.id,
                todaysWotdDate = today,
            )
        }
        return word
    }

    suspend fun resolveVisibleOratorIds(): List<Long> =
        resolveVisibleOratorIds(userPreferencesDao.getUserPreferences().orDefault())

    suspend fun resolveLibraryOratorIds(): List<Long> =
        resolveLibraryOratorIds(userPreferencesDao.getUserPreferences().orDefault())

    /**
     * Catalog-visible orators narrowed to starred favorites when the user
     * rotates through a curated set (the first-run onboarding default).
     */
    suspend fun resolveLibraryOratorIds(preferences: UserPreferencesEntity): List<Long> {
        val visible = resolveVisibleOratorIds(preferences)
        return LearningPool.rotationOratorIds(
            visibleOratorIds = visible,
            rotateThroughAll = preferences.rotateThroughAll,
            favoriteOratorIds = preferences.favoriteOratorIds,
        )
    }

    suspend fun resolveVisibleOratorIds(preferences: UserPreferencesEntity): List<Long> {
        return dictionaryDao.getAllDictionaries()
            .asSequence()
            .filter { it.isActive }
            .filter { dictionary ->
                OratorCatalogKind.isVisible(
                    category = dictionary.category,
                    includeLiterary = preferences.includeLiteraryOrators,
                    includeFictional = preferences.includeFictionalOrators,
                )
            }
            .map { it.id }
            .toList()
    }

    private suspend fun loadPool(
        oratorId: Long?,
        favoriteOratorIds: List<Long>,
        visibleOratorIds: Collection<Long>,
    ): List<WordEntity> {
        return when {
            oratorId != null -> wordDao.getWordsByOrator(oratorId)
            favoriteOratorIds.isNotEmpty() -> wordDao.getWordsByOratorIds(favoriteOratorIds)
            visibleOratorIds.isNotEmpty() -> wordDao.getWordsByOratorIds(visibleOratorIds.toList())
            else -> emptyList()
        }
    }

    suspend fun getRandomWords(
        limit: Int,
        oratorId: Long?,
        visibleOratorIds: Collection<Long>? = null,
    ): List<WordEntity> {
        val visible = visibleOratorIds ?: resolveVisibleOratorIds()
        return when {
            oratorId != null -> wordDao.getRandomWordsByOrator(oratorId, limit)
            visible.isNotEmpty() -> wordDao.getRandomWordsByOratorIds(visible.toList(), limit)
            else -> emptyList()
        }
    }

    suspend fun getRandomSavedWords(limit: Int): List<WordEntity> = wordDao.getRandomSavedWords(limit)

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
        savedOnly: Boolean = false,
        visibleOratorIds: Collection<Long>? = null,
    ): WordEntity? {
        fun letterCountOk(word: WordEntity): Boolean {
            val letters = word.word.filter { it.isLetter() }
            return letters.isNotEmpty() && letters.length in minLetters..maxLetters
        }

        val visible = visibleOratorIds ?: resolveVisibleOratorIds()

        suspend fun pool(orator: Long?): List<WordEntity> {
            val raw = when {
                savedOnly -> wordDao.getRandomSavedWords(poolSize)
                orator != null -> wordDao.getRandomWordsPoolByOrator(orator, poolSize)
                visible.isNotEmpty() -> wordDao.getRandomWordsPoolByOratorIds(visible.toList(), poolSize)
                else -> emptyList()
            }
            return raw.filter(::letterCountOk)
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

        if (!savedOnly && oratorId != null) {
            pick(pool(null))?.let { return it }
        }

        if (savedOnly) return null

        val lastResort = if (visible.isNotEmpty()) {
            wordDao.getRandomWordsPoolByOratorIds(visible.toList(), poolSize * 3)
        } else {
            emptyList()
        }
        return pick(lastResort.filter(::letterCountOk))
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

}
