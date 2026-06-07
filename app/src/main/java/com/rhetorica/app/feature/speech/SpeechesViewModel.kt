package com.rhetorica.app.feature.speech

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.SpeechDao
import com.rhetorica.app.data.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SpeechesViewModel @Inject constructor(
    private val speechDao: SpeechDao,
    private val dictionaryRepository: DictionaryRepository,
) : ViewModel() {

    val uiState: StateFlow<SpeechesUiState> = combine(
        speechDao.observeAllSpeeches(),
        dictionaryRepository.observeActiveOratorProfiles(),
    ) { speeches, orators ->
        SpeechesUiState(
            speeches = speeches,
            orators = orators,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpeechesUiState(isLoading = true),
    )
}

data class SpeechesUiState(
    val speeches: List<SpeechEntity> = emptyList(),
    val orators: List<OratorProfile> = emptyList(),
    val isLoading: Boolean = false,
)
