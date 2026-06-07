package com.rhetorica.app.feature.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.model.WordThemes
import com.rhetorica.app.core.ui.WordListCard

@Composable
fun SavedRoute(
    onWordClick: (Long) -> Unit,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SavedScreen(
        state = state,
        onWordClick = onWordClick,
        onToggleSaved = viewModel::toggleSaved,
        onSelectOrator = viewModel::selectOrator,
        onSelectSort = viewModel::selectSort,
        onToggleCategory = viewModel::toggleCategory,
        onClearCategories = viewModel::clearCategoryFilter,
    )
}

@Composable
private fun SavedScreen(
    state: SavedUiState,
    onWordClick: (Long) -> Unit,
    onToggleSaved: (Long) -> Unit,
    onSelectOrator: (Long?) -> Unit,
    onSelectSort: (SavedSortOption) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClearCategories: () -> Unit,
) {
    val isTrulyEmpty = state.totalSavedCount == 0
    val isFilteredEmpty = state.words.isEmpty() && !isTrulyEmpty

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (isTrulyEmpty) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.saved_empty_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.saved_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.saved_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.saved_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.saved_count,
                        if (state.selectedOratorId == null) state.totalSavedCount else state.words.size,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item {
            SavedControls(
                state = state,
                onSelectOrator = onSelectOrator,
                onSelectSort = onSelectSort,
                onToggleCategory = onToggleCategory,
                onClearCategories = onClearCategories,
            )
        }

        if (isFilteredEmpty) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.saved_filtered_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = if (state.selectedOratorName != null) {
                            stringResource(
                                R.string.saved_filtered_empty_body_orator,
                                state.selectedOratorName,
                            )
                        } else {
                            stringResource(R.string.saved_filtered_empty_body)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.words, key = { it.id }) { word ->
                WordListCard(
                    word = word.word,
                    partOfSpeech = word.partOfSpeech,
                    definition = word.definition,
                    example = word.example,
                    isSaved = true,
                    onClick = { onWordClick(word.id) },
                    onToggleSaved = { onToggleSaved(word.id) },
                    source = word.source,
                    speech = word.speech,
                    categories = word.categories,
                )
            }
        }
    }
}

@Composable
private fun SavedControls(
    state: SavedUiState,
    onSelectOrator: (Long?) -> Unit,
    onSelectSort: (SavedSortOption) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClearCategories: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.saved_filter_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedOratorId == null,
                        onClick = { onSelectOrator(null) },
                        label = { Text(text = stringResource(R.string.saved_filter_all)) },
                    )
                }
                items(state.availableOrators, key = { it.id }) { orator ->
                    FilterChip(
                        selected = state.selectedOratorId == orator.id,
                        onClick = { onSelectOrator(orator.id) },
                        label = { Text(text = orator.name) },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.saved_themes_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedCategories.isEmpty(),
                        onClick = { onClearCategories() },
                        label = { Text(text = stringResource(R.string.saved_themes_all)) },
                    )
                }
                // Always offer the canonical theme categories so the UI always references them
                val canonical = WordThemes.canonicalList()
                val themeChips = canonical + state.availableCategories.filterNot { it in canonical }
                items(themeChips.distinct(), key = { it }) { cat ->
                    val label = WordThemes.displayName(cat)
                    FilterChip(
                        selected = cat in state.selectedCategories,
                        onClick = { onToggleCategory(cat) },
                        label = { Text(text = label) },
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.saved_sort_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SavedSortOption.entries) { sortOption ->
                    FilterChip(
                        selected = state.selectedSort == sortOption,
                        onClick = { onSelectSort(sortOption) },
                        label = {
                            Text(
                                text = when (sortOption) {
                                    SavedSortOption.Newest -> stringResource(R.string.saved_sort_newest)
                                    SavedSortOption.Alphabetical -> stringResource(R.string.saved_sort_alphabetical)
                                    SavedSortOption.PartOfSpeech -> stringResource(R.string.saved_sort_part_of_speech)
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}
