package com.vocabdaily.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocabdaily.app.data.local.WordEntity
import com.vocabdaily.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: WordRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = repository.observeWords()
        .map { words -> HomeUiState(words = words, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true),
        )

    init {
        viewModelScope.launch {
            repository.seedWordsIfEmpty()
        }
    }
}

data class HomeUiState(
    val words: List<WordEntity> = emptyList(),
    val isLoading: Boolean = false,
)
