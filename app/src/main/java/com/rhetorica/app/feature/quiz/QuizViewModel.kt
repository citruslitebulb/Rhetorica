package com.rhetorica.app.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.data.repository.WordOfDaySelector
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuizMode {
    MultipleChoice,
    WordGuess,
}

/**
 * Letter-guess difficulty bands.
 *
 * - Easy: short words, more attempts, length shown
 * - Medium: mid-length words, standard attempts, length shown
 * - Hard: longer words, fewer attempts, length shown
 * - Hardcore: any length hidden, fewest attempts
 */
enum class WordGuessDifficulty(
    val minLetters: Int,
    val maxLetters: Int,
    val revealsLength: Boolean,
    val maxAttempts: Int,
) {
    Easy(minLetters = 4, maxLetters = 5, revealsLength = true, maxAttempts = 6),
    Medium(minLetters = 5, maxLetters = 6, revealsLength = true, maxAttempts = 5),
    Hard(minLetters = 7, maxLetters = 10, revealsLength = true, maxAttempts = 4),
    Hardcore(minLetters = 4, maxLetters = 14, revealsLength = false, maxAttempts = 3),
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val progressRepository: ProgressRepository,
    private val userPreferencesDao: UserPreferencesDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState(isLoading = true))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /** Cancels in-flight letter-guess loads so a slow previous pick cannot overwrite a newer one. */
    private var wordGuessJob: Job? = null

    init {
        loadForCurrentMode()
    }

    fun setMode(mode: QuizMode) {
        if (_uiState.value.mode == mode) return
        _uiState.update {
            it.copy(
                mode = mode,
                // Keep session score across mode switches within the same visit.
            )
        }
        loadForCurrentMode()
    }

    fun setDifficulty(difficulty: WordGuessDifficulty) {
        if (_uiState.value.difficulty == difficulty) return
        _uiState.update { it.copy(difficulty = difficulty) }
        if (_uiState.value.mode == QuizMode.WordGuess) {
            startWordGuess()
        }
    }

    fun loadForCurrentMode() {
        when (_uiState.value.mode) {
            QuizMode.MultipleChoice -> loadMultipleChoice()
            QuizMode.WordGuess -> startWordGuess()
        }
    }

    // region Multiple choice

    fun loadMultipleChoice() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    mode = QuizMode.MultipleChoice,
                    isLoading = true,
                    isUnavailable = false,
                    selectedOptionId = null,
                    isAnswered = false,
                    options = emptyList(),
                    promptDefinition = "",
                    // clear word-guess board noise when switching
                    guessTarget = "",
                    guessTargetDisplay = "",
                    submittedGuesses = emptyList(),
                    currentGuess = "",
                    keyboardMarks = emptyMap(),
                    wordGuessStatus = WordGuessStatus.Playing,
                    maxAttempts = it.difficulty.maxAttempts,
                )
            }

            val preferences = userPreferencesDao.getUserPreferences()
            val oratorId = WordOfDaySelector.resolveOratorId(
                selectedOratorId = preferences?.selectedOratorId,
                rotateThroughAll = preferences?.rotateThroughAll ?: false,
            )

            var pool = wordRepository.getRandomWords(limit = 16, oratorId = oratorId)
            if (pool.size < MIN_OPTIONS) {
                pool = wordRepository.getRandomWords(limit = 16, oratorId = null)
            }

            if (pool.size < MIN_OPTIONS) {
                _uiState.update {
                    it.copy(isLoading = false, isUnavailable = true)
                }
                return@launch
            }

            val correct = pool.random()
            val distractors = pool
                .filter { it.id != correct.id }
                .shuffled()
                .take(MIN_OPTIONS - 1)
            val options = (distractors + correct).shuffled()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isUnavailable = false,
                    promptDefinition = correct.definition,
                    correctWordId = correct.id,
                    options = options.map { w ->
                        QuizOption(id = w.id, word = w.word, partOfSpeech = w.partOfSpeech)
                    },
                    selectedOptionId = null,
                    isAnswered = false,
                )
            }
        }
    }

    fun selectOption(optionId: Long) {
        val state = _uiState.value
        if (state.mode != QuizMode.MultipleChoice || state.isAnswered || state.isLoading) return

        val isCorrect = optionId == state.correctWordId
        viewModelScope.launch {
            if (isCorrect) progressRepository.recordQuizCorrect()
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

    fun nextMultipleChoice() {
        loadMultipleChoice()
    }

    // endregion

    // region Word guess (Wordle-like)

    fun startWordGuess() {
        wordGuessJob?.cancel()
        wordGuessJob = viewModelScope.launch {
            val previous = _uiState.value
            val difficulty = previous.difficulty
            val excludeIds = buildSet {
                if (previous.correctWordId > 0L) add(previous.correctWordId)
            }
            val excludeDefs = buildSet {
                previous.promptDefinition.trim().lowercase()
                    .takeIf { it.isNotEmpty() }
                    ?.let { add(it) }
            }

            // Clear the previous round immediately so the UI never shows a mismatched
            // definition + answer while the next word is loading.
            _uiState.update {
                it.copy(
                    mode = QuizMode.WordGuess,
                    isLoading = true,
                    isUnavailable = false,
                    promptDefinition = "",
                    correctWordId = -1L,
                    guessTarget = "",
                    guessTargetDisplay = "",
                    targetLength = 0,
                    currentGuess = "",
                    submittedGuesses = emptyList(),
                    keyboardMarks = emptyMap(),
                    wordGuessStatus = WordGuessStatus.Playing,
                    maxAttempts = difficulty.maxAttempts,
                    revealsLength = difficulty.revealsLength,
                    message = null,
                )
            }

            val preferences = userPreferencesDao.getUserPreferences()
            val oratorId = WordOfDaySelector.resolveOratorId(
                selectedOratorId = preferences?.selectedOratorId,
                rotateThroughAll = preferences?.rotateThroughAll ?: false,
            )

            val word = wordRepository.getRandomWordForLetterGuess(
                oratorId = oratorId,
                minLetters = difficulty.minLetters,
                maxLetters = difficulty.maxLetters,
                excludeWordIds = excludeIds,
                excludeDefinitions = excludeDefs,
            )

            ensureActive()

            if (word == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isUnavailable = true,
                        promptDefinition = "",
                        guessTarget = "",
                        guessTargetDisplay = "",
                    )
                }
                return@launch
            }

            val target = WordGuessEvaluator.normalize(word.word)
            if (target.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isUnavailable = true) }
                return@launch
            }

            // Single atomic publish of definition + answer so they always match.
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isUnavailable = false,
                    promptDefinition = word.definition,
                    correctWordId = word.id,
                    guessTarget = target,
                    guessTargetDisplay = word.word,
                    targetLength = target.length,
                    revealsLength = difficulty.revealsLength,
                    maxAttempts = difficulty.maxAttempts,
                    currentGuess = "",
                    submittedGuesses = emptyList(),
                    keyboardMarks = emptyMap(),
                    wordGuessStatus = WordGuessStatus.Playing,
                    message = null,
                )
            }
        }
    }

    fun onKeyPress(char: Char) {
        val state = _uiState.value
        if (state.mode != QuizMode.WordGuess) return
        if (state.wordGuessStatus != WordGuessStatus.Playing) return
        if (!char.isLetter()) return

        val letter = char.lowercaseChar()
        val maxLen = if (state.revealsLength) {
            state.targetLength
        } else {
            MAX_UNKNOWN_INPUT_LENGTH
        }
        if (state.currentGuess.length >= maxLen) return

        _uiState.update {
            it.copy(
                currentGuess = it.currentGuess + letter,
                message = null,
            )
        }
    }

    fun onBackspace() {
        val state = _uiState.value
        if (state.mode != QuizMode.WordGuess) return
        if (state.wordGuessStatus != WordGuessStatus.Playing) return
        if (state.currentGuess.isEmpty()) return
        _uiState.update {
            it.copy(
                currentGuess = it.currentGuess.dropLast(1),
                message = null,
            )
        }
    }

    fun onSubmitGuess() {
        val state = _uiState.value
        if (state.mode != QuizMode.WordGuess) return
        if (state.wordGuessStatus != WordGuessStatus.Playing) return
        if (state.isLoading) return

        val guess = WordGuessEvaluator.normalize(state.currentGuess)
        val target = state.guessTarget
        if (target.isEmpty()) return
        if (guess.isEmpty()) return

        if (state.revealsLength) {
            // Fixed-length modes: require exact length before spending an attempt.
            if (guess.length != state.targetLength) {
                _uiState.update {
                    it.copy(message = GuessMessage.NeedExactLength(state.targetLength))
                }
                return
            }
        } else {
            // Hardcore: length is unknown — any non-empty guess spends an attempt.
            // Wrong-length guesses still get letter feedback (no early reject).
            if (guess.length < MIN_UNKNOWN_GUESS_LENGTH) {
                _uiState.update {
                    it.copy(message = GuessMessage.NeedMinLength(MIN_UNKNOWN_GUESS_LENGTH))
                }
                return
            }
        }

        val marks = if (state.revealsLength) {
            WordGuessEvaluator.evaluate(guess, target)
        } else {
            WordGuessEvaluator.evaluateAllowingLengthMismatch(guess, target)
        }
        val row = GuessRow(letters = guess, marks = marks)
        val submitted = state.submittedGuesses + row
        val keyboard = WordGuessEvaluator.mergeKeyboardState(state.keyboardMarks, guess, marks)
        // Must match full word (length + every letter) to win.
        val won = guess == target
        val lost = !won && submitted.size >= state.maxAttempts

        if (won) {
            viewModelScope.launch { progressRepository.recordQuizCorrect() }
        }

        _uiState.update {
            it.copy(
                submittedGuesses = submitted,
                currentGuess = "",
                keyboardMarks = keyboard,
                wordGuessStatus = when {
                    won -> WordGuessStatus.Won
                    lost -> WordGuessStatus.Lost
                    else -> WordGuessStatus.Playing
                },
                sessionCorrect = it.sessionCorrect + if (won) 1 else 0,
                sessionTotal = it.sessionTotal + if (won || lost) 1 else 0,
                message = null,
            )
        }
    }

    fun nextWordGuess() {
        startWordGuess()
    }

    // endregion

    companion object {
        private const val MIN_OPTIONS = 4
        private const val MAX_UNKNOWN_INPUT_LENGTH = 14
        private const val MIN_UNKNOWN_GUESS_LENGTH = 3
    }
}

data class QuizUiState(
    val mode: QuizMode = QuizMode.WordGuess,
    val difficulty: WordGuessDifficulty = WordGuessDifficulty.Easy,
    val isLoading: Boolean = false,
    val isUnavailable: Boolean = false,
    // Shared session stats
    val sessionCorrect: Int = 0,
    val sessionTotal: Int = 0,
    // Multiple choice
    val promptDefinition: String = "",
    val correctWordId: Long = -1L,
    val options: List<QuizOption> = emptyList(),
    val selectedOptionId: Long? = null,
    val isAnswered: Boolean = false,
    // Word guess
    val guessTarget: String = "",
    val guessTargetDisplay: String = "",
    val targetLength: Int = 0,
    val revealsLength: Boolean = true,
    val currentGuess: String = "",
    val submittedGuesses: List<GuessRow> = emptyList(),
    val keyboardMarks: Map<Char, LetterMark> = emptyMap(),
    val wordGuessStatus: WordGuessStatus = WordGuessStatus.Playing,
    val maxAttempts: Int = WordGuessDifficulty.Easy.maxAttempts,
    val message: GuessMessage? = null,
)

data class QuizOption(
    val id: Long,
    val word: String,
    val partOfSpeech: String,
)

data class GuessRow(
    val letters: String,
    val marks: List<LetterMark>,
)

enum class WordGuessStatus {
    Playing,
    Won,
    Lost,
}

sealed class GuessMessage {
    data class NeedExactLength(val length: Int) : GuessMessage()
    data class NeedMinLength(val length: Int) : GuessMessage()
}
