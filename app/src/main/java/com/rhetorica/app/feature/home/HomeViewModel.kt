package com.rhetorica.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.repository.DictionaryRepository
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

        // Determine orators that match the selected themes (if any)
        val themeMatchingOratorIds: Set<Long> = if (selectedThemes.isEmpty()) {
            emptySet() // means "all"
        } else {
            orators.filter { orator ->
                orator.themeCategories.any { it in activeThemeSet }
            }.map { it.id }.toSet()
        }

        // Effective orator for filtering:
        // - If specific orator selected and it matches themes (or no themes), use it
        // - Else if rotate, use null (all, but restricted below to theme matching)
        val effectiveOratorId = if (rotateThroughAll) {
            null
        } else {
            selectedOratorId?.takeIf { id ->
                themeMatchingOratorIds.isEmpty() || id in themeMatchingOratorIds
            }
        }

        var filteredWords = words

        if (effectiveOratorId != null) {
            filteredWords = filteredWords.filter { it.oratorId == effectiveOratorId }
        } else if (themeMatchingOratorIds.isNotEmpty()) {
            filteredWords = filteredWords.filter { (it.oratorId ?: 0L) in themeMatchingOratorIds }
        }

        // Word-level theme filter (in addition to orator themes)
        val activeCategories = activeThemeSet
        if (activeCategories.isNotEmpty()) {
            filteredWords = filteredWords.filter { word ->
                word.categories.any { cat -> cat in activeCategories }
            }
        }

        val availableCategories = words
            .flatMap { it.categories }
            .distinct()
            .sorted()

        HomeUiState(
            words = filteredWords.map { word ->
                HomeWordCardState(
                    word = word,
                    isSaved = word.id in savedWordIds,
                )
            },
            availableCategories = availableCategories,
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
            } catch (e: Exception) {
                // Log error and update UI state if needed
                android.util.Log.e("HomeViewModel", "Failed to fix orator IDs", e)
            }
        }
    }

    fun toggleSaved(wordId: Long) {
        viewModelScope.launch {
            repository.toggleSaved(wordId)
        }
    }
}

data class HomeUiState(
    val words: List<HomeWordCardState> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class HomeWordCardState(
    val word: WordEntity,
    val isSaved: Boolean,
)
