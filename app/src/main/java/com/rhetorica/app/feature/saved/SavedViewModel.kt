package com.rhetorica.app.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.SavedWordSummary
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: WordRepository,
) : ViewModel() {
    val uiState: StateFlow<SavedUiState> = repository.observeSavedWordSummaries()
        .map { words ->
            SavedUiState(words = words, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SavedUiState(isLoading = true),
        )

    fun toggleSaved(wordId: Long) {
        viewModelScope.launch {
            repository.toggleSaved(wordId)
        }
    }
}

data class SavedUiState(
    val words: List<SavedWordSummary> = emptyList(),
    val isLoading: Boolean = false,
)
