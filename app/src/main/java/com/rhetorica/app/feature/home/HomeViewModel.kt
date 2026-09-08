package com.rhetorica.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.HabitProgress
import com.rhetorica.app.core.model.LearningPool
import com.rhetorica.app.core.model.OratorCatalogKind.Companion.filterByCatalog
import com.rhetorica.app.core.util.AppLog
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.local.orDefault
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.data.repository.WordOfDaySelector
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WordRepository,
    private val userPreferencesDao: UserPreferencesDao,
    private val dictionaryRepository: DictionaryRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {
    private val feedQuery = combine(
        userPreferencesDao.observeUserPreferences(),
        dictionaryRepository.observeActiveOratorProfiles(),
    ) { preferences, orators ->
        val prefs = preferences.orDefault()
        val visibleOrators = orators.filterByCatalog(
            includeLiterary = prefs.includeLiteraryOrators,
            includeFictional = prefs.includeFictionalOrators,
        )
        val scope = LearningPool.feedScope(
            visibleOrators = visibleOrators,
            selectedOratorId = prefs.selectedOratorId,
            rotateThroughAll = prefs.rotateThroughAll,
            favoriteOratorIds = prefs.favoriteOratorIds,
            selectedThemes = prefs.selectedThemeCategories,
        )
        val selectedOratorId = prefs.selectedOratorId?.takeIf { id ->
            visibleOrators.any { it.id == id }
        }
        val rotateThroughAll = prefs.rotateThroughAll

        val wotdOratorId = WordOfDaySelector.resolveOratorId(
            selectedOratorId = selectedOratorId,
            rotateThroughAll = rotateThroughAll,
            visibleOratorIds = visibleOrators.map { it.id },
        )
        FeedQuery(
            feedOratorId = scope.feedOratorId,
            scopedOratorIds = scope.scopedOratorIds,
            activeThemeSet = scope.activeThemeSet,
            hasActiveFilters = scope.hasActiveFilters,
            wotdOratorId = wotdOratorId,
            todaysWotdId = prefs.todaysWotdId,
            todaysWotdDate = prefs.todaysWotdDate,
            oratorNameById = visibleOrators.associate { it.id to it.name },
        )
    }

    val uiState: StateFlow<HomeUiState> = feedQuery.flatMapLatest { query ->
        combine(
            repository.observeWordsForFeed(query.feedOratorId, query.scopedOratorIds),
            repository.observeSavedWordIds(),
            progressRepository.observeIsWordOpened(query.todaysWotdId ?: -1L),
            repository.observeWordById(query.todaysWotdId ?: -1L),
        ) { words, savedWordIds, openedTodaysWord, todaysWord ->
            var filteredWords = words
            if (query.activeThemeSet.isNotEmpty()) {
                filteredWords = filteredWords.filter { word ->
                    word.categories.any { cat -> cat in query.activeThemeSet }
                }
            }

            val wordOfTheDay = todaysWord
            val resolvedWotdOratorId = query.wotdOratorId ?: wordOfTheDay?.oratorId
            val wotdOratorName = resolvedWotdOratorId?.let { query.oratorNameById[it] }
            val listWords = if (wordOfTheDay != null) {
                filteredWords.filter { it.id != wordOfTheDay.id }
            } else {
                filteredWords
            }

            HomeUiState(
                words = listWords.map { word ->
                    HomeWordCardState(
                        word = word,
                        isSaved = word.id in savedWordIds,
                    )
                },
                wordOfTheDay = wordOfTheDay?.let { wotd ->
                    HomeWordCardState(
                        word = wotd,
                        isSaved = wotd.id in savedWordIds,
                    )
                },
                wordOfTheDayOratorId = resolvedWotdOratorId,
                wordOfTheDayOratorName = wotdOratorName,
                openedTodaysWord = HabitProgress.isTodaysWordOpened(
                    todaysWotdId = query.todaysWotdId,
                    todaysWotdDate = query.todaysWotdDate,
                    isOpened = openedTodaysWord,
                ),
                totalWordCount = words.size,
                browseWordCount = listWords.size,
                hasActiveFilters = query.hasActiveFilters,
                availableCategories = words.flatMap { it.categories }.distinct().sorted(),
                selectedCategories = query.activeThemeSet,
                isLoading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            try {
                repository.fixNullOratorIds()
                progressRepository.syncSavedCount()
            } catch (e: Exception) {
                AppLog.e("HomeViewModel", "Failed during home init", e)
            }
        }
        viewModelScope.launch {
            userPreferencesDao.observeUserPreferences().collect { raw ->
                val prefs = raw.orDefault()
                val today = LocalDate.now(ZoneId.systemDefault()).toString()
                if (WordOfDaySelector.needsNewPick(prefs.todaysWotdId, prefs.todaysWotdDate, today)) {
                    try {
                        repository.ensureTodaysWord(today = today)
                    } catch (e: Exception) {
                        AppLog.e("HomeViewModel", "Failed to persist Word of the Day", e)
                    }
                }
            }
        }
    }

    fun toggleSaved(wordId: Long) {
        viewModelScope.launch {
            repository.toggleSaved(wordId)
            progressRepository.syncSavedCount()
        }
    }
}

private data class FeedQuery(
    val feedOratorId: Long?,
    val scopedOratorIds: Collection<Long>?,
    val activeThemeSet: Set<String>,
    val hasActiveFilters: Boolean,
    val wotdOratorId: Long?,
    val todaysWotdId: Long?,
    val todaysWotdDate: String,
    val oratorNameById: Map<Long, String>,
)

data class HomeUiState(
    val words: List<HomeWordCardState> = emptyList(),
    val wordOfTheDay: HomeWordCardState? = null,
    val wordOfTheDayOratorId: Long? = null,
    val wordOfTheDayOratorName: String? = null,
    val openedTodaysWord: Boolean = false,
    val totalWordCount: Int = 0,
    val browseWordCount: Int = 0,
    val hasActiveFilters: Boolean = false,
    val availableCategories: List<String> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class HomeWordCardState(
    val word: WordEntity,
    val isSaved: Boolean,
)
