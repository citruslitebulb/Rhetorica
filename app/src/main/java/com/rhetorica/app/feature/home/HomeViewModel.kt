package com.rhetorica.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.data.repository.WordOfDaySelector
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WordRepository,
    private val userPreferencesDao: UserPreferencesDao,
    private val dictionaryRepository: DictionaryRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesDao.observeUserPreferences(),
        repository.observeWords(),
        repository.observeSavedWordIds(),
        dictionaryRepository.observeActiveOratorProfiles(),
    ) { preferences, words, savedWordIds, orators ->
        val selectedOratorId = preferences?.selectedOratorId
        val rotateThroughAll = preferences?.rotateThroughAll ?: false
        val selectedThemes = preferences?.selectedThemeCategories ?: emptyList()
        val activeThemeSet = selectedThemes.toSet()
        val hasActiveFilters = selectedThemes.isNotEmpty() ||
            (!rotateThroughAll && selectedOratorId != null)

        val themeMatchingOratorIds: Set<Long> = if (selectedThemes.isEmpty()) {
            emptySet()
        } else {
            orators.filter { orator ->
                orator.themeCategories.any { it in activeThemeSet }
            }.map { it.id }.toSet()
        }

        // Feed filter may consider themes + orator selection.
        val feedOratorId = if (rotateThroughAll) {
            null
        } else {
            selectedOratorId?.takeIf { id ->
                themeMatchingOratorIds.isEmpty() || id in themeMatchingOratorIds
            }
        }

        var filteredWords = words

        if (feedOratorId != null) {
            filteredWords = filteredWords.filter { it.oratorId == feedOratorId }
        } else if (themeMatchingOratorIds.isNotEmpty()) {
            filteredWords = filteredWords.filter { (it.oratorId ?: 0L) in themeMatchingOratorIds }
        }

        if (activeThemeSet.isNotEmpty()) {
            filteredWords = filteredWords.filter { word ->
                word.categories.any { cat -> cat in activeThemeSet }
            }
        }

        // Word of the Day is owned by the selected orator (or all, if rotating).
        // Themes never reassign the WotD to a different orator's word.
        val wotdOratorId = WordOfDaySelector.resolveOratorId(
            selectedOratorId = selectedOratorId,
            rotateThroughAll = rotateThroughAll,
        )
        val wordOfTheDay = WordOfDaySelector.select(
            allWords = words,
            oratorId = wotdOratorId,
        )

        val oratorNameById = orators.associate { it.id to it.name }
        // Always show the orator who actually owns the word (which is the selected one when set).
        val wotdOratorName = when {
            wotdOratorId != null -> oratorNameById[wotdOratorId]
            else -> wordOfTheDay?.oratorId?.let { oratorNameById[it] }
        }

        // Keep the daily word out of the main list when present to avoid duplication.
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
            wordOfTheDayOratorName = wotdOratorName,
            totalWordCount = words.size,
            hasActiveFilters = hasActiveFilters,
            availableCategories = words.flatMap { it.categories }.distinct().sorted(),
            selectedCategories = activeThemeSet,
            isLoading = false,
        )
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
                android.util.Log.e("HomeViewModel", "Failed during home init", e)
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

data class HomeUiState(
    val words: List<HomeWordCardState> = emptyList(),
    val wordOfTheDay: HomeWordCardState? = null,
    val wordOfTheDayOratorName: String? = null,
    val totalWordCount: Int = 0,
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
