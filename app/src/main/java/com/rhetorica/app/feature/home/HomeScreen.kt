package com.rhetorica.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.ui.WordListCard

@Composable
fun HomeRoute(
    onWordClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onWordClick = onWordClick,
        onToggleSaved = viewModel::toggleSaved,
    )
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onWordClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
) {
    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 24.dp))
        }
        return
    }

    val isLibraryEmpty = state.totalWordCount == 0
    val isFilterEmpty = !isLibraryEmpty &&
        state.words.isEmpty() &&
        state.wordOfTheDay == null

    if (isLibraryEmpty) {
        EmptyState(
            title = stringResource(R.string.home_empty_title),
            body = stringResource(R.string.home_empty_body),
        )
        return
    }

    if (isFilterEmpty) {
        EmptyState(
            title = stringResource(R.string.home_filtered_empty_title),
            body = stringResource(R.string.home_filtered_empty_body),
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.wordOfTheDay?.let { wotd ->
            item(key = "word_of_the_day") {
                WordOfTheDayHero(
                    state = wotd,
                    oratorName = state.wordOfTheDayOratorName,
                    onClick = { onWordClick(wotd.word.id) },
                    onToggleSaved = { onToggleSaved(wotd.word.id) },
                )
            }
        }

        if (state.words.isNotEmpty()) {
            item(key = "browse_header") {
                Text(
                    text = stringResource(R.string.home_browse_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                )
            }

            items(state.words, key = { it.word.id }) { item ->
                WordListCard(
                    word = item.word.word,
                    partOfSpeech = item.word.partOfSpeech,
                    definition = item.word.definition,
                    example = item.word.example,
                    isSaved = item.isSaved,
                    onClick = { onWordClick(item.word.id) },
                    onToggleSaved = { onToggleSaved(item.word.id) },
                    source = item.word.source,
                    speech = item.word.speech,
                    categories = item.word.categories,
                )
            }
        }
    }
}

@Composable
private fun WordOfTheDayHero(
    state: HomeWordCardState,
    oratorName: String?,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    val word = state.word
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_wotd_badge),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
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
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = word.partOfSpeech,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                oratorName?.let { name ->
                    Text(
                        text = "· $name",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
            Text(
                text = word.definition,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (word.example.isNotBlank()) {
                Text(
                    text = word.example,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
