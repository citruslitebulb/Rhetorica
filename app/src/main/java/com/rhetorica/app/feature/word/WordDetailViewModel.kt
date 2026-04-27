package com.rhetorica.app.feature.word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WordRepository,
) : ViewModel() {
    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    val uiState: StateFlow<WordDetailUiState> = combine(
        repository.observeWordById(wordId),
        repository.observeIsWordSaved(wordId),
    ) { word, isSaved ->
        WordDetailUiState(
            word = word,
            isSaved = isSaved,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WordDetailUiState(isLoading = true),
    )

    fun toggleSaved() {
        viewModelScope.launch {
            repository.toggleSaved(wordId)
        }
    }
}

data class WordDetailUiState(
    val word: WordEntity? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
)
