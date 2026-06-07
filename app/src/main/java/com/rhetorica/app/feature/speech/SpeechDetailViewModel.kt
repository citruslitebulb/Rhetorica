package com.rhetorica.app.feature.speech

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.SpeechEntity
import com.rhetorica.app.data.local.SpeechDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SpeechDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val speechDao: SpeechDao,
) : ViewModel() {
    private val oratorId: Long = checkNotNull(savedStateHandle["oratorId"])
    private val speechTitle: String = checkNotNull(
        savedStateHandle.get<String>("speechTitle")?.let(Uri::decode)
    )

    val uiState: StateFlow<SpeechDetailUiState> = speechDao
        .observeSpeechesByOrator(oratorId)
        .map { speeches ->
            val speech = findMatchingSpeech(speeches, speechTitle)
            SpeechDetailUiState(
                speech = speech,
                isLoading = false,
                requestedOratorId = oratorId,
                requestedTitle = speechTitle,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SpeechDetailUiState(isLoading = true),
        )
}

data class SpeechDetailUiState(
    val speech: SpeechEntity? = null,
    val isLoading: Boolean = false,
    val requestedOratorId: Long = 0L,
    val requestedTitle: String = "",
)

internal fun findMatchingSpeech(speeches: List<SpeechEntity>, requestedTitle: String): SpeechEntity? {
    val normalizedRequested = normalizeSpeechTitle(requestedTitle)
    return speeches.firstOrNull { it.title.equals(requestedTitle, ignoreCase = true) }
        ?: speeches.firstOrNull {
            val normalizedStored = normalizeSpeechTitle(it.title)
            normalizedRequested == normalizedStored
                || requestedTitle.contains(it.title, ignoreCase = true)
                || it.title.contains(requestedTitle, ignoreCase = true)
                || normalizedRequested.contains(normalizedStored)
                || normalizedStored.contains(normalizedRequested)
        }
}

private fun normalizeSpeechTitle(title: String): String =
    title.lowercase()
        .replace(Regex("""['"]"""), "")
        .replace(Regex("""\s*\([^)]*\)"""), "")
        .replace(Regex("""\s+"""), " ")
        .trim()
