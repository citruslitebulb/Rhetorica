package com.rhetorica.app.feature.word

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rhetorica.app.R
import com.rhetorica.app.core.model.WordThemes

const val wordDetailRoute = "word/{wordId}"

fun NavController.navigateToWordDetail(wordId: Long) {
    navigate("word/$wordId") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.wordDetailScreen(
    onBack: () -> Unit,
    onReadFullSpeech: (Long, String) -> Unit = { _, _ -> },
) {
    // Deep links (rhetorica://word/{id}) are handled in MainActivity to avoid double-nav
    // when both NavHost deepLinks and Activity intent handling fire.
    composable(
        route = wordDetailRoute,
        arguments = listOf(
            navArgument("wordId") { type = NavType.LongType },
        ),
    ) {
        WordDetailRoute(onBack = onBack, onReadFullSpeech = onReadFullSpeech)
    }
}

@Composable
fun WordDetailRoute(
    onBack: () -> Unit,
    onReadFullSpeech: (Long, String) -> Unit = { _, _ -> },
    viewModel: WordDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WordDetailScreen(
        state = state,
        onBack = onBack,
        onToggleSaved = viewModel::toggleSaved,
        onSpeak = viewModel::speakWord,
        onReadFullSpeech = onReadFullSpeech,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WordDetailScreen(
    state: WordDetailUiState,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onSpeak: () -> Unit,
    onReadFullSpeech: (Long, String) -> Unit = { _, _ -> },
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.word_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (state.word != null) {
                        IconButton(onClick = onSpeak) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = stringResource(R.string.word_speak),
                            )
                        }
                        IconButton(onClick = onToggleSaved) {
                            Icon(
                                imageVector = if (state.isSaved) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = if (state.isSaved) {
                                    stringResource(R.string.word_unsave)
                                } else {
                                    stringResource(R.string.word_save)
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.notFound || state.word == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.word_not_found),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            else -> {
                val word = state.word
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = word.word,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = word.partOfSpeech,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = if (state.isSaved) {
                                    stringResource(R.string.word_saved_state)
                                } else {
                                    stringResource(R.string.word_unsaved_state)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (word.categories.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            word.categories.forEach { cat ->
                                val label = WordThemes.displayName(cat)
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            RoundedCornerShape(6.dp),
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }

                    DetailBlock(
                        title = stringResource(R.string.word_definition_title),
                        body = word.definition,
                    )

                    DetailBlock(
                        title = stringResource(R.string.word_example_title),
                        body = word.example,
                        italic = true,
                    )

                    if (word.source != null || word.speech != null) {
                        val attribution = buildString {
                            val title = word.speech ?: word.source
                            if (title != null) append("— ").append(title)
                            if (word.speech != null && word.source != null && word.source != word.speech) {
                                append(" (").append(word.source).append(")")
                            }
                        }.trim()
                        Text(
                            text = attribution,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        )
                    }

                    if (word.speech != null && (word.oratorId ?: 0L) != 0L) {
                        OutlinedButton(
                            onClick = { onReadFullSpeech(word.oratorId!!, word.speech) },
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Text(text = stringResource(R.string.word_read_full_speech))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBlock(
    title: String,
    body: String,
    italic: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        )
    }
}
