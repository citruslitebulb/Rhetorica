package com.rhetorica.app.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R

@Composable
fun QuizRoute(
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuizScreen(
        state = state,
        onModeSelected = viewModel::setMode,
        onDifficultySelected = viewModel::setDifficulty,
        onSelectOption = viewModel::selectOption,
        onNextMultipleChoice = viewModel::nextMultipleChoice,
        onRetry = viewModel::loadForCurrentMode,
        onKeyPress = viewModel::onKeyPress,
        onBackspace = viewModel::onBackspace,
        onSubmitGuess = viewModel::onSubmitGuess,
        onNextWordGuess = viewModel::nextWordGuess,
    )
}

@Composable
private fun QuizScreen(
    state: QuizUiState,
    onModeSelected: (QuizMode) -> Unit,
    onDifficultySelected: (WordGuessDifficulty) -> Unit,
    onSelectOption: (Long) -> Unit,
    onNextMultipleChoice: () -> Unit,
    onRetry: () -> Unit,
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmitGuess: () -> Unit,
    onNextWordGuess: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.quiz_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        ModeSelector(
            mode = state.mode,
            onModeSelected = onModeSelected,
        )

        if (state.sessionTotal > 0) {
            Text(
                text = stringResource(
                    R.string.quiz_score,
                    state.sessionCorrect,
                    state.sessionTotal,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        when {
            // Always show a spinner while a round is loading so definition/answer
            // never flash as a mismatched pair from the previous round.
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.isUnavailable -> {
                UnavailableBlock(
                    body = if (state.mode == QuizMode.WordGuess) {
                        stringResource(R.string.quiz_word_guess_unavailable_body)
                    } else {
                        stringResource(R.string.quiz_unavailable_body)
                    },
                    onRetry = onRetry,
                )
            }

            state.mode == QuizMode.MultipleChoice -> {
                MultipleChoiceContent(
                    state = state,
                    onSelectOption = onSelectOption,
                    onNext = onNextMultipleChoice,
                )
            }

            else -> {
                WordGuessContent(
                    state = state,
                    onDifficultySelected = onDifficultySelected,
                    onKeyPress = onKeyPress,
                    onBackspace = onBackspace,
                    onSubmitGuess = onSubmitGuess,
                    onNext = onNextWordGuess,
                )
            }
        }
    }
}

@Composable
private fun ModeSelector(
    mode: QuizMode,
    onModeSelected: (QuizMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = mode == QuizMode.MultipleChoice,
            onClick = { onModeSelected(QuizMode.MultipleChoice) },
            label = { Text(stringResource(R.string.quiz_mode_multiple_choice)) },
        )
        FilterChip(
            selected = mode == QuizMode.WordGuess,
            onClick = { onModeSelected(QuizMode.WordGuess) },
            label = { Text(stringResource(R.string.quiz_mode_word_guess)) },
        )
    }
}

@Composable
private fun UnavailableBlock(
    body: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.quiz_unavailable_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(R.string.quiz_retry))
        }
    }
}

@Composable
private fun MultipleChoiceContent(
    state: QuizUiState,
    onSelectOption: (Long) -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.quiz_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        DefinitionCard(definition = state.promptDefinition)

        state.options.forEach { option ->
            val isSelected = state.selectedOptionId == option.id
            val isCorrect = option.id == state.correctWordId
            val containerColor = when {
                !state.isAnswered -> MaterialTheme.colorScheme.surface
                isCorrect -> MaterialTheme.colorScheme.primaryContainer
                isSelected -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }

            Card(
                onClick = { onSelectOption(option.id) },
                enabled = !state.isAnswered,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = containerColor),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.word,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = option.partOfSpeech,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.isAnswered && isCorrect) {
                        Text(
                            text = stringResource(R.string.quiz_correct_badge),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        if (state.isAnswered) {
            val correct = state.selectedOptionId == state.correctWordId
            Text(
                text = if (correct) {
                    stringResource(R.string.quiz_feedback_correct)
                } else {
                    stringResource(R.string.quiz_feedback_incorrect)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (correct) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.quiz_next))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordGuessContent(
    state: QuizUiState,
    onDifficultySelected: (WordGuessDifficulty) -> Unit,
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmitGuess: () -> Unit,
    onNext: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.quiz_word_guess_subtitle, state.maxAttempts),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.quiz_difficulty_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            DifficultyChip(
                label = stringResource(R.string.quiz_difficulty_easy),
                selected = state.difficulty == WordGuessDifficulty.Easy,
                onClick = { onDifficultySelected(WordGuessDifficulty.Easy) },
            )
            DifficultyChip(
                label = stringResource(R.string.quiz_difficulty_medium),
                selected = state.difficulty == WordGuessDifficulty.Medium,
                onClick = { onDifficultySelected(WordGuessDifficulty.Medium) },
            )
            DifficultyChip(
                label = stringResource(R.string.quiz_difficulty_hard),
                selected = state.difficulty == WordGuessDifficulty.Hard,
                onClick = { onDifficultySelected(WordGuessDifficulty.Hard) },
            )
            DifficultyChip(
                label = stringResource(R.string.quiz_difficulty_hardcore),
                selected = state.difficulty == WordGuessDifficulty.Hardcore,
                onClick = { onDifficultySelected(WordGuessDifficulty.Hardcore) },
            )
        }

        DefinitionCard(definition = state.promptDefinition)

        GuessBoard(state = state)

        state.message?.let { msg ->
            val text = when (msg) {
                is GuessMessage.NeedExactLength ->
                    stringResource(R.string.quiz_guess_need_length, msg.length)
                is GuessMessage.NeedMinLength ->
                    stringResource(R.string.quiz_guess_need_min, msg.length)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        when (state.wordGuessStatus) {
            WordGuessStatus.Won -> {
                Text(
                    text = stringResource(R.string.quiz_guess_won, state.guessTargetDisplay),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.quiz_guess_next))
                }
            }
            WordGuessStatus.Lost -> {
                Text(
                    text = stringResource(R.string.quiz_guess_lost, state.guessTargetDisplay),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.quiz_guess_next))
                }
            }
            WordGuessStatus.Playing -> Unit
        }

        VirtualKeyboard(
            keyboardMarks = state.keyboardMarks,
            enabled = state.wordGuessStatus == WordGuessStatus.Playing,
            onKeyPress = onKeyPress,
            onBackspace = onBackspace,
            onSubmit = onSubmitGuess,
        )
    }
}

@Composable
private fun DifficultyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun DefinitionCard(definition: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.quiz_prompt_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = definition,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun GuessBoard(state: QuizUiState) {
    val rows = buildList {
        addAll(state.submittedGuesses)
        if (state.wordGuessStatus == WordGuessStatus.Playing) {
            // Active input row (unmarked)
            add(
                GuessRow(
                    letters = if (state.revealsLength) {
                        state.currentGuess.padEnd(state.targetLength, ' ')
                    } else {
                        state.currentGuess.ifEmpty { " " }
                    },
                    marks = emptyList(),
                ),
            )
        }
        // Pad remaining attempt slots for a stable Wordle-like grid.
        val empties = state.maxAttempts - size
        if (state.revealsLength) {
            repeat(empties.coerceAtLeast(0)) {
                add(GuessRow(letters = " ".repeat(state.targetLength.coerceAtLeast(1)), marks = emptyList()))
            }
        } else {
            repeat(empties.coerceAtLeast(0)) {
                add(GuessRow(letters = " ", marks = emptyList()))
            }
        }
    }.take(state.maxAttempts)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEachIndexed { index, row ->
            val isActive =
                state.wordGuessStatus == WordGuessStatus.Playing &&
                    index == state.submittedGuesses.size
            GuessRowView(
                row = row,
                isActive = isActive,
                fixedLength = if (state.revealsLength) state.targetLength else null,
            )
        }
    }
}

@Composable
private fun GuessRowView(
    row: GuessRow,
    isActive: Boolean,
    fixedLength: Int?,
) {
    val letters = if (fixedLength != null) {
        row.letters.padEnd(fixedLength, ' ').take(fixedLength)
    } else {
        row.letters
    }
    val marked = row.marks.isNotEmpty()

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        letters.forEachIndexed { i, ch ->
            val mark = row.marks.getOrNull(i)
            LetterTile(
                char = ch,
                mark = mark,
                isActive = isActive && !marked,
            )
        }
    }
}

@Composable
private fun LetterTile(
    char: Char,
    mark: LetterMark?,
    isActive: Boolean,
) {
    val bg = when (mark) {
        LetterMark.CORRECT -> Color(0xFF538D4E)
        LetterMark.PRESENT -> Color(0xFFB59F3B)
        LetterMark.ABSENT -> Color(0xFF3A3A3C)
        LetterMark.UNUSED, null -> {
            if (isActive && char.isLetter()) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
        }
    }
    val borderColor = when {
        mark != null && mark != LetterMark.UNUSED -> bg
        isActive && char.isLetter() -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    val textColor = when (mark) {
        LetterMark.CORRECT, LetterMark.PRESENT, LetterMark.ABSENT -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (char.isLetter()) {
            Text(
                text = char.uppercaseChar().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
        }
    }
}

@Composable
private fun VirtualKeyboard(
    keyboardMarks: Map<Char, LetterMark>,
    enabled: Boolean,
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
) {
    val rows = listOf(
        "qwertyuiop",
        "asdfghjkl",
        "zxcvbnm",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        rows.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (rowIndex == 2) {
                    KeyboardActionKey(
                        label = stringResource(R.string.quiz_enter),
                        enabled = enabled,
                        wide = true,
                        onClick = onSubmit,
                    )
                }
                row.forEach { ch ->
                    KeyboardLetterKey(
                        letter = ch,
                        mark = keyboardMarks[ch] ?: LetterMark.UNUSED,
                        enabled = enabled,
                        onClick = { onKeyPress(ch) },
                    )
                }
                if (rowIndex == 2) {
                    KeyboardActionKey(
                        label = stringResource(R.string.quiz_delete),
                        enabled = enabled,
                        wide = true,
                        onClick = onBackspace,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyboardLetterKey(
    letter: Char,
    mark: LetterMark,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = when (mark) {
        LetterMark.CORRECT -> Color(0xFF538D4E)
        LetterMark.PRESENT -> Color(0xFFB59F3B)
        LetterMark.ABSENT -> Color(0xFF3A3A3C)
        LetterMark.UNUSED -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when (mark) {
        LetterMark.CORRECT, LetterMark.PRESENT, LetterMark.ABSENT -> Color.White
        LetterMark.UNUSED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg.copy(alpha = if (enabled) 1f else 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.uppercaseChar().toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun KeyboardActionKey(
    label: String,
    enabled: Boolean,
    wide: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = if (wide) 48.dp else 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(
                    alpha = if (enabled) 1f else 0.5f,
                ),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 11.sp,
        )
    }
}
