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
    private val selectedCategories = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<SavedUiState> = combine(
        repository.observeSavedWordSummaries(),
        dictionaryRepository.observeActiveOratorProfiles(),
        selectedOratorId,
        selectedSort,
        selectedCategories,
    ) { words, orators, currentOratorId, currentSort, currentCategories ->
        val filteredWords = words
            .filter { currentOratorId == null || it.oratorId == currentOratorId }
            .filter { currentCategories.isEmpty() || it.categories.any { cat -> cat in currentCategories } }
            .let { currentSort.sort(it) }
        val selectedOratorName = orators.firstOrNull { it.id == currentOratorId }?.name

        val availableCategories = words
            .flatMap { it.categories }
            .distinct()
            .sorted()

        SavedUiState(
            words = filteredWords,
            availableOrators = orators,
            selectedOratorId = currentOratorId,
            selectedOratorName = selectedOratorName,
            selectedSort = currentSort,
            selectedCategories = currentCategories,
            availableCategories = availableCategories,
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

    fun toggleCategory(category: String) {
        selectedCategories.update { current ->
            if (category in current) current - category else current + category
        }
    }

    fun clearCategoryFilter() {
        selectedCategories.value = emptySet()
    }
}

data class SavedUiState(
    val words: List<SavedWordSummary> = emptyList(),
    val availableOrators: List<OratorProfile> = emptyList(),
    val selectedOratorId: Long? = null,
    val selectedOratorName: String? = null,
    val selectedSort: SavedSortOption = SavedSortOption.Newest,
    val availableCategories: List<String> = emptyList(),
    val selectedCategories: Set<String> = emptySet(),
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
