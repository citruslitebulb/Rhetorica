package com.rhetorica.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val wordRepository: WordRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            query.debounce(200).distinctUntilChanged().collect { text ->
                if (text.trim().length < 2) {
                    _uiState.update {
                        it.copy(query = text, words = emptyList(), speeches = emptyList(), isSearching = false)
                    }
                    return@collect
                }
                _uiState.update { it.copy(query = text, isSearching = true) }
                val words = wordRepository.searchWords(text)
                val speeches = wordRepository.searchSpeeches(text)
                _uiState.update {
                    it.copy(query = text, words = words, speeches = speeches, isSearching = false)
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
        _uiState.update { it.copy(query = value) }
    }
}

data class SearchUiState(
    val query: String = "",
    val words: List<WordEntity> = emptyList(),
    val speeches: List<SpeechEntity> = emptyList(),
    val isSearching: Boolean = false,
)
