package com.rhetorica.app.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.data.repository.WordOfDaySelector
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository,
    private val userPreferencesDao: UserPreferencesDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState(isLoading = true))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestion()
    }

    fun loadQuestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedOptionId = null, isAnswered = false) }

            val preferences = userPreferencesDao.getUserPreferences()
            val oratorId = WordOfDaySelector.resolveOratorId(
                selectedOratorId = preferences?.selectedOratorId,
                rotateThroughAll = preferences?.rotateThroughAll ?: false,
            )

            // Prefer selected orator; fall back to full library when the pool is too small
            // for a fair 4-option multiple-choice question (not only when empty).
            var pool = wordRepository.getRandomWords(limit = 16, oratorId = oratorId)
            if (pool.size < MIN_OPTIONS) {
                pool = wordRepository.getRandomWords(limit = 16, oratorId = null)
            }

            if (pool.size < MIN_OPTIONS) {
                _uiState.value = QuizUiState(
                    isLoading = false,
                    isUnavailable = true,
                    sessionCorrect = _uiState.value.sessionCorrect,
                    sessionTotal = _uiState.value.sessionTotal,
                )
                return@launch
            }

            val correct = pool.random()
            val distractors = pool
                .filter { it.id != correct.id }
                .shuffled()
                .take(MIN_OPTIONS - 1)
            val options = (distractors + correct).shuffled()

            _uiState.value = QuizUiState(
                isLoading = false,
                isUnavailable = false,
                promptDefinition = correct.definition,
                correctWordId = correct.id,
                options = options.map {
                    QuizOption(id = it.id, word = it.word, partOfSpeech = it.partOfSpeech)
                },
                sessionCorrect = _uiState.value.sessionCorrect,
                sessionTotal = _uiState.value.sessionTotal,
            )
        }
    }

    fun selectOption(optionId: Long) {
        val state = _uiState.value
        if (state.isAnswered || state.isLoading) return

        val isCorrect = optionId == state.correctWordId
        viewModelScope.launch {
            if (isCorrect) {
                progressRepository.recordQuizCorrect()
            }
        }

        _uiState.update {
            it.copy(
                selectedOptionId = optionId,
                isAnswered = true,
                sessionCorrect = it.sessionCorrect + if (isCorrect) 1 else 0,
                sessionTotal = it.sessionTotal + 1,
            )
        }
    }

    fun nextQuestion() {
        loadQuestion()
    }

    companion object {
        private const val MIN_OPTIONS = 4
    }
}

data class QuizUiState(
    val isLoading: Boolean = false,
    val isUnavailable: Boolean = false,
    val promptDefinition: String = "",
    val correctWordId: Long = -1L,
    val options: List<QuizOption> = emptyList(),
    val selectedOptionId: Long? = null,
    val isAnswered: Boolean = false,
    val sessionCorrect: Int = 0,
    val sessionTotal: Int = 0,
)

data class QuizOption(
    val id: Long,
    val word: String,
    val partOfSpeech: String,
)
