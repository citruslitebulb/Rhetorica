package com.rhetorica.app.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.SavedWordSummary
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: WordRepository,
    private val dictionaryRepository: DictionaryRepository,
) : ViewModel() {
    private val selectedOratorId = MutableStateFlow<Long?>(null)
    private val selectedSort = MutableStateFlow(SavedSortOption.Newest)

    val uiState: StateFlow<SavedUiState> = combine(
        repository.observeSavedWordSummaries(),
        dictionaryRepository.observeActiveOratorProfiles(),
        selectedOratorId,
        selectedSort,
    ) { words, orators, currentOratorId, currentSort ->
        val filteredWords = words
            .filter { currentOratorId == null || it.oratorId == currentOratorId }
            .let { currentSort.sort(it) }
        val selectedOratorName = orators.firstOrNull { it.id == currentOratorId }?.name

        SavedUiState(
            words = filteredWords,
            availableOrators = orators,
            selectedOratorId = currentOratorId,
            selectedOratorName = selectedOratorName,
            selectedSort = currentSort,
            totalSavedCount = words.size,
            isLoading = false,
        )
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

    fun selectOrator(oratorId: Long?) {
        selectedOratorId.value = oratorId
    }

    fun selectSort(sortOption: SavedSortOption) {
        selectedSort.update { sortOption }
    }
}

data class SavedUiState(
    val words: List<SavedWordSummary> = emptyList(),
    val availableOrators: List<OratorProfile> = emptyList(),
    val selectedOratorId: Long? = null,
    val selectedOratorName: String? = null,
    val selectedSort: SavedSortOption = SavedSortOption.Newest,
    val totalSavedCount: Int = 0,
    val isLoading: Boolean = false,
)

enum class SavedSortOption {
    Newest,
    Alphabetical,
    PartOfSpeech,
    ;

    fun sort(words: List<SavedWordSummary>): List<SavedWordSummary> {
        return when (this) {
            Newest -> words.sortedByDescending { it.savedAtEpochMillis }
            Alphabetical -> words.sortedBy { it.word.lowercase() }
            PartOfSpeech -> words.sortedWith(
                compareBy<SavedWordSummary> { it.partOfSpeech.lowercase() }
                    .thenBy { it.word.lowercase() },
            )
        }
    }
}
