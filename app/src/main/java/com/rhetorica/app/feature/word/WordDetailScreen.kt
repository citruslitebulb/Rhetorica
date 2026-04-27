package com.rhetorica.app.feature.word

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

const val wordDetailRoute = "word/{wordId}"

fun NavController.navigateToWordDetail(wordId: Long) {
    navigate("word/$wordId")
}

fun NavGraphBuilder.wordDetailScreen(
    onBack: () -> Unit,
) {
    composable(
        route = wordDetailRoute,
        arguments = listOf(
            navArgument("wordId") { type = NavType.LongType },
        ),
    ) {
        WordDetailRoute(onBack = onBack)
    }
}

@Composable
fun WordDetailRoute(
    onBack: () -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WordDetailScreen(
        state = state,
        onBack = onBack,
        onToggleSaved = viewModel::toggleSaved,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WordDetailScreen(
    state: WordDetailUiState,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
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

            state.word == null -> {
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

                    DetailBlock(
                        title = stringResource(R.string.word_definition_title),
                        body = word.definition,
                    )

                    DetailBlock(
                        title = stringResource(R.string.word_example_title),
                        body = word.example,
                        italic = true,
                    )
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
