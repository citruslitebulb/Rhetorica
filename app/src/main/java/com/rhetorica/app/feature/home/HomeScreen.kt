package com.rhetorica.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 24.dp))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
            )
        }
    }
}
