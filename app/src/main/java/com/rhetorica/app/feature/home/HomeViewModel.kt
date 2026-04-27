package com.rhetorica.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.WordEntity
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
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesDao.observeUserPreferences(),
        repository.observeWords(),
        repository.observeSavedWordIds(),
    ) { preferences, words, savedWordIds ->
        val selectedOratorId = preferences?.selectedOratorId
        val rotateThroughAll = preferences?.rotateThroughAll ?: false
        val effectiveOratorId = if (rotateThroughAll) null else selectedOratorId

        val filteredWords = if (effectiveOratorId == null) {
            words
        } else {
            words.filter { it.oratorId == effectiveOratorId }
        }

        HomeUiState(
            words = filteredWords.map { word ->
                HomeWordCardState(
                    word = word,
                    isSaved = word.id in savedWordIds,
                )
            },
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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class HomeWordCardState(
    val word: WordEntity,
    val isSaved: Boolean,
)
