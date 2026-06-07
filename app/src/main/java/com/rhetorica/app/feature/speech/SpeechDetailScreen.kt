package com.rhetorica.app.feature.speech

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rhetorica.app.R

const val speechDetailRoute = "speech/{oratorId}?title={speechTitle}"

fun NavController.navigateToFullSpeech(oratorId: Long, speechTitle: String) {
    val encodedTitle = Uri.encode(speechTitle)
    navigate("speech/$oratorId?title=$encodedTitle")
}

fun NavGraphBuilder.speechDetailScreen(
    onBack: () -> Unit,
) {
    composable(
        route = speechDetailRoute,
        arguments = listOf(
            navArgument("oratorId") { type = NavType.LongType },
            navArgument("speechTitle") { type = NavType.StringType },
        ),
    ) {
        SpeechDetailRoute(onBack = onBack)
    }
}

@Composable
fun SpeechDetailRoute(
    onBack: () -> Unit,
    viewModel: SpeechDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SpeechDetailScreen(
        state = state,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SpeechDetailScreen(
    state: SpeechDetailUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = state.speech?.title ?: stringResource(R.string.speech_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.speech == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.speech_not_found),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Requested: \"${state.requestedTitle}\" (orator ${state.requestedOratorId})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.speech_not_found_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                val speech = state.speech
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = speech.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (speech.year != null) {
                        Text(
                            text = speech.year.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (speech.description != null) {
                        Text(
                            text = speech.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = speech.fullText,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5,
                    )
                }
            }
        }
    }
}
