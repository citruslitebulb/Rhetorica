package com.rhetorica.app.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.ui.OratorPortrait
import com.rhetorica.app.core.ui.WordListCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun HomeRoute(
    onWordClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onWordClick = onWordClick,
        onToggleSaved = viewModel::toggleSaved,
        onSettingsClick = onSettingsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState,
    onWordClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.home_settings),
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
                InfiniteHomeFeed(
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
}

/**
 * Word of the Day fills the first screen. Browse vocabulary sits below the fold,
 * fades in as the user scrolls, and loops forever via modular virtualization.
 *
 * List layout:
 * - index 0: WotD hero
 * - indices 1..virtualCount: browse words (content = words[(i-1) % size])
 */
@Composable
private fun InfiniteHomeFeed(
    state: HomeUiState,
    onWordClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val browseWords = state.words
    val listState = rememberLazyListState()
    val period = browseWords.size.coerceAtLeast(1)
    val virtualCount = if (browseWords.isEmpty()) {
        0
    } else {
        (period * LOOP_PERIODS).coerceAtMost(MAX_VIRTUAL_ITEMS)
    }

    MaintainInfiniteWindow(
        listState = listState,
        browseSize = browseWords.size,
        virtualCount = virtualCount,
    )

    val browseReveal by remember {
        derivedStateOf {
            when {
                browseWords.isEmpty() -> 0f
                listState.firstVisibleItemIndex == 0 -> {
                    (listState.firstVisibleItemScrollOffset / BROWSE_FADE_SCROLL_PX)
                        .coerceIn(0f, 1f)
                }
                else -> 1f
            }
        }
    }
    val browseAlpha by animateFloatAsState(
        targetValue = browseReveal,
        animationSpec = tween(durationMillis = 180),
        label = "browseAlpha",
    )

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "word_of_the_day") {
            Column(
                modifier = Modifier
                    .fillParentMaxHeight(0.92f)
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                state.wordOfTheDay?.let { wotd ->
                    WordOfTheDayHero(
                        state = wotd,
                        oratorName = state.wordOfTheDayOratorName,
                        onClick = { onWordClick(wotd.word.id) },
                        onToggleSaved = { onToggleSaved(wotd.word.id) },
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OratorPortrait(
                        oratorId = state.wordOfTheDayOratorId ?: wotd.word.oratorId,
                        oratorName = state.wordOfTheDayOratorName,
                        size = 168.dp,
                    )
                }
            }
        }

        if (browseWords.isNotEmpty()) {
            items(
                count = virtualCount,
                key = { virtualIndex -> "loop_$virtualIndex" },
            ) { virtualIndex ->
                val item = browseWords[virtualIndex % browseWords.size]
                // Softer stagger for items still near the hero during the fade-in.
                val distanceFromHero = if (listState.firstVisibleItemIndex == 0) {
                    virtualIndex
                } else {
                    (virtualIndex - (listState.firstVisibleItemIndex - 1)).coerceAtLeast(0)
                }
                val stagger = 1f - (distanceFromHero * 0.08f).coerceIn(0f, 0.35f)
                val itemAlpha = (browseAlpha * stagger).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = itemAlpha
                        translationY = (1f - itemAlpha) * 18f
                    },
                ) {
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
}

/**
 * When scrolling near the end of the virtual browse window, jump backward by whole
 * library periods so the list never hits a hard bottom. Upward scroll to the WotD
 * hero (list index 0) is left alone.
 */
@Composable
private fun MaintainInfiniteWindow(
    listState: LazyListState,
    browseSize: Int,
    virtualCount: Int,
) {
    if (browseSize <= 0 || virtualCount <= 0) return
    val period = browseSize
    val highEdge = virtualCount - period * EDGE_PERIODS

    LaunchedEffect(listState, browseSize, virtualCount) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .filter { (first, _) -> first >= 1 }
            .collect { (first, offset) ->
                val browseIndex = first - 1
                if (browseIndex > highEdge) {
                    val mid = virtualCount / 2
                    val aligned = mid - (mid % period) + (browseIndex % period)
                    val targetListIndex = (aligned + 1).coerceIn(1, virtualCount)
                    listState.scrollToItem(
                        index = targetListIndex,
                        scrollOffset = offset,
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

private const val LOOP_PERIODS = 400
private const val MAX_VIRTUAL_ITEMS = 50_000
private const val EDGE_PERIODS = 3
/** Scroll distance (px) over which browse vocabulary fades fully in. */
private const val BROWSE_FADE_SCROLL_PX = 280f
