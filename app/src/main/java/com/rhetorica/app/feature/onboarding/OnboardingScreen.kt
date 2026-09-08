package com.rhetorica.app.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.core.model.OratorVoiceFamily
import com.rhetorica.app.core.model.WordThemes

private const val PAGE_WELCOME = 0
private const val PAGE_INTERESTS = 1
private const val PAGE_HABIT = 2
private const val PAGE_COUNT = 3

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingPager(
        state = state,
        onToggleFamily = viewModel::toggleFamily,
        onToggleTheme = viewModel::toggleTheme,
        onClearThemes = viewModel::clearThemes,
        onToggleNotifications = viewModel::setNotificationsEnabled,
        onPinWidget = viewModel::requestPinWidget,
        onFinish = { viewModel.completeOnboarding(onFinished) },
        notificationPermissionGranted = viewModel.notificationPermissionGranted(),
    )
}

@Composable
private fun OnboardingPager(
    state: OnboardingUiState,
    onToggleFamily: (OratorVoiceFamily) -> Unit,
    onToggleTheme: (String) -> Unit,
    onClearThemes: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onPinWidget: () -> Unit,
    onFinish: () -> Unit,
    notificationPermissionGranted: Boolean,
) {
    var page by rememberSaveable { mutableIntStateOf(PAGE_WELCOME) }
    val canAdvance = page != PAGE_INTERESTS || state.canContinueFromVoices

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.onboarding_step, page + 1, PAGE_COUNT),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { (page + 1) / PAGE_COUNT.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (page) {
                PAGE_WELCOME -> WelcomePage()
                PAGE_INTERESTS -> InterestsPage(
                    state = state,
                    onToggleFamily = onToggleFamily,
                    onToggleTheme = onToggleTheme,
                    onClearThemes = onClearThemes,
                )
                else -> HabitPage(
                    state = state,
                    onToggleNotifications = onToggleNotifications,
                    onPinWidget = onPinWidget,
                    notificationPermissionGranted = notificationPermissionGranted,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (page > PAGE_WELCOME) {
                OutlinedButton(
                    onClick = { page -= 1 },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.onboarding_back))
                }
            }
            Button(
                onClick = {
                    if (page < PAGE_HABIT) page += 1 else onFinish()
                },
                enabled = canAdvance,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (page < PAGE_HABIT) {
                        stringResource(R.string.onboarding_next)
                    } else {
                        stringResource(R.string.onboarding_done)
                    },
                )
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_questions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestsPage(
    state: OnboardingUiState,
    onToggleFamily: (OratorVoiceFamily) -> Unit,
    onToggleTheme: (String) -> Unit,
    onClearThemes: () -> Unit,
) {
    if (state.orators.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "voices-header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.onboarding_voices_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.onboarding_voices_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.selectedFamilies.isEmpty()) {
                    Text(
                        text = stringResource(R.string.onboarding_voices_need_one),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        items(OratorVoiceFamily.entries, key = { it.name }) { family ->
            val members = state.orators.filter { OratorVoiceFamily.from(it) == family }
            VoiceFamilyCard(
                family = family,
                selected = family in state.selectedFamilies,
                members = members,
                onToggle = { onToggleFamily(family) },
            )
        }
        item(key = "themes-section") {
            Column(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_themes_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.onboarding_themes_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.selectedThemes.isEmpty(),
                        onClick = onClearThemes,
                        label = { Text(text = stringResource(R.string.saved_themes_all)) },
                    )
                    WordThemes.canonicalList().forEach { theme ->
                        FilterChip(
                            selected = theme in state.selectedThemes,
                            onClick = { onToggleTheme(theme) },
                            label = { Text(text = WordThemes.displayName(theme)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceFamilyCard(
    family: OratorVoiceFamily,
    selected: Boolean,
    members: List<OratorProfile>,
    onToggle: () -> Unit,
) {
    val examples = members.take(3).joinToString { it.name }
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(familyTitleRes(family)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(familyBodyRes(family)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (examples.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.onboarding_family_examples, examples),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (members.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.onboarding_family_count, members.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun HabitPage(
    state: OnboardingUiState,
    onToggleNotifications: (Boolean) -> Unit,
    onPinWidget: () -> Unit,
    notificationPermissionGranted: Boolean,
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onToggleNotifications(granted)
    }

    LaunchedEffect(state.notificationsEnabled) {
        if (state.notificationsEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_habit_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_habit_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_notifications_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(
                        R.string.profile_notification_time_value,
                        state.notificationHour,
                        state.notificationMinute,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.notificationsEnabled,
                onCheckedChange = { checked ->
                    if (checked &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !notificationPermissionGranted &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onToggleNotifications(checked)
                    }
                },
            )
        }
        if (state.canPinWidget) {
            OutlinedButton(onClick = onPinWidget, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.onboarding_add_widget))
            }
        } else {
            Text(
                text = stringResource(R.string.onboarding_widget_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun familyTitleRes(family: OratorVoiceFamily): Int = when (family) {
    OratorVoiceFamily.Classical -> R.string.onboarding_family_classical_title
    OratorVoiceFamily.Statesmen -> R.string.onboarding_family_statesmen_title
    OratorVoiceFamily.Justice -> R.string.onboarding_family_justice_title
    OratorVoiceFamily.Literary -> R.string.onboarding_family_literary_title
    OratorVoiceFamily.Technology -> R.string.onboarding_family_technology_title
    OratorVoiceFamily.Fictional -> R.string.onboarding_family_fictional_title
}

private fun familyBodyRes(family: OratorVoiceFamily): Int = when (family) {
    OratorVoiceFamily.Classical -> R.string.onboarding_family_classical_body
    OratorVoiceFamily.Statesmen -> R.string.onboarding_family_statesmen_body
    OratorVoiceFamily.Justice -> R.string.onboarding_family_justice_body
    OratorVoiceFamily.Literary -> R.string.onboarding_family_literary_body
    OratorVoiceFamily.Technology -> R.string.onboarding_family_technology_body
    OratorVoiceFamily.Fictional -> R.string.onboarding_family_fictional_body
}
