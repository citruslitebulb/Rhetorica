package com.rhetorica.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onWordClick = onWordClick,
        onToggleSaved = viewModel::toggleSaved,
        onSearchClick = onSearchClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    onWordClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
    onSearchClick: () -> Unit,
) {
    val context = LocalContext.current
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf("") }
    var mailError by rememberSaveable { mutableStateOf(false) }
    val address = stringResource(R.string.feedback_destination_email)
    val subject = stringResource(R.string.feedback_subject)

    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = {
                    sheetOpen = true
                    mailError = false
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Feedback,
                    contentDescription = stringResource(R.string.feedback_cd),
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search_title),
                        )
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
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(horizontal = 24.dp))
                }
            }

            state.totalWordCount == 0 -> {
                EmptyState(
                    title = stringResource(R.string.home_empty_title),
                    body = stringResource(R.string.home_empty_body),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            state.wordOfTheDay == null && state.words.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.home_filtered_empty_title),
                    body = stringResource(R.string.home_filtered_empty_body),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            else -> {
                HomeFeed(
                    state = state,
                    onWordClick = onWordClick,
                    onToggleSaved = onToggleSaved,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background),
                )
            }
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                sheetOpen = false
                message = ""
                mailError = false
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.feedback_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        mailError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    label = { Text(stringResource(R.string.feedback_message_label)) },
                )
                if (mailError) {
                    Text(
                        text = stringResource(R.string.feedback_mail_missing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(
                    onClick = {
                        val launched = FeedbackMail.launch(
                            context = context,
                            address = address,
                            subject = subject,
                            body = message.trim(),
                        )
                        if (launched) {
                            sheetOpen = false
                            message = ""
                            mailError = false
                        } else {
                            mailError = true
                        }
                    },
                    enabled = FeedbackMail.hasMessage(message),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.feedback_submit))
                }
            }
        }
    }
}

/**
 * Word of the Day sits at the top; browse vocabulary follows on the same
 * screen so the feed matches the gold-card home mockup.
 */
@Composable
private fun HomeFeed(
    state: HomeUiState,
    onWordClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val browseWords = state.words

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.wordOfTheDay?.let { wotd ->
            item(key = "word_of_the_day") {
                WordOfTheDayHero(
                    state = wotd,
                    oratorName = state.wordOfTheDayOratorName,
                    openedToday = state.openedTodaysWord,
                    onClick = { onWordClick(wotd.word.id) },
                    onToggleSaved = { onToggleSaved(wotd.word.id) },
                )
            }
        }

        if (browseWords.isNotEmpty()) {
            item(key = "browse_title") {
                Text(
                    text = stringResource(R.string.home_browse_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(
                count = browseWords.size,
                key = { index -> "word_${browseWords[index].word.id}" },
            ) { index ->
                val item = browseWords[index]
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
            item(key = "feed_end") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_feed_end_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(
                                R.string.home_feed_end_body,
                                state.browseWordCount,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordOfTheDayHero(
    state: HomeWordCardState,
    oratorName: String?,
    openedToday: Boolean,
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
        shape = RoundedCornerShape(20.dp),
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
                    text = if (openedToday) {
                        stringResource(R.string.home_wotd_opened)
                    } else {
                        stringResource(R.string.home_wotd_badge)
                    },
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
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = buildString {
                    append(word.partOfSpeech)
                    if (!oratorName.isNullOrBlank()) {
                        append(" · ")
                        append(oratorName)
                    }
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
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
