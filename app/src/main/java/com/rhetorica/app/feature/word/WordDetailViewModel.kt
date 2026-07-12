package com.rhetorica.app.feature.word

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.tts.TtsSpeaker
import com.rhetorica.app.data.local.QuoteDao
import com.rhetorica.app.data.local.QuoteEntity
import com.rhetorica.app.data.local.WordEntity
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: WordRepository,
    private val quoteDao: QuoteDao,
    private val progressRepository: ProgressRepository,
    private val ttsSpeaker: TtsSpeaker,
) : ViewModel() {
    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])
    private val relatedQuote = MutableStateFlow<QuoteEntity?>(null)
    /** True after the first Room emission for this wordId (null or entity). */
    private val hasResolved = MutableStateFlow(false)

    val uiState: StateFlow<WordDetailUiState> = combine(
        repository.observeWordById(wordId),
        repository.observeIsWordSaved(wordId),
        relatedQuote,
        hasResolved,
    ) { word, isSaved, quote, resolved ->
        WordDetailUiState(
            word = word,
            isSaved = isSaved,
            relatedQuote = quote,
            isLoading = !resolved,
            notFound = resolved && word == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WordDetailUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            // Do not use filterNotNull().first() — missing words would hang forever.
            val word = repository.observeWordById(wordId).first()
            hasResolved.value = true
            if (word == null) return@launch

            progressRepository.recordWordViewed()

            val oratorId = word.oratorId
            if (oratorId != null && oratorId != 0L) {
                relatedQuote.value = resolveRelatedQuote(
                    oratorId = oratorId,
                    speech = word.speech,
                    source = word.source,
                )
            }
        }
    }

    private suspend fun resolveRelatedQuote(
        oratorId: Long,
        speech: String?,
        source: String?,
    ): QuoteEntity? {
        if (!speech.isNullOrBlank() || !source.isNullOrBlank()) {
            quoteDao.getQuoteMatchingSpeechOrSource(
                oratorId = oratorId,
                speech = speech,
                source = source,
            )?.let { return it }
        }
        return quoteDao.getRandomQuoteByOrator(oratorId)
    }

    fun toggleSaved() {
        viewModelScope.launch {
            repository.toggleSaved(wordId)
            progressRepository.syncSavedCount()
        }
    }

    fun speakWord() {
        val word = uiState.value.word ?: return
        ttsSpeaker.speakWordAndDefinition(word.word, word.definition)
    }
}

data class WordDetailUiState(
    val word: WordEntity? = null,
    val isSaved: Boolean = false,
    val relatedQuote: QuoteEntity? = null,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
)
