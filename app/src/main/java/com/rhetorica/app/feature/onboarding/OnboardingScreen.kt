package com.rhetorica.app.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.ui.OratorPortrait

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingPager(
        state = state,
        onSelectOrator = viewModel::selectOrator,
        onRotateAll = viewModel::setRotateThroughAll,
        onToggleNotifications = viewModel::setNotificationsEnabled,
        onPinWidget = viewModel::requestPinWidget,
        onFinish = { viewModel.completeOnboarding(onFinished) },
        notificationPermissionGranted = viewModel.notificationPermissionGranted(),
    )
}

@Composable
private fun OnboardingPager(
    state: OnboardingUiState,
    onSelectOrator: (Long) -> Unit,
    onRotateAll: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onPinWidget: () -> Unit,
    onFinish: () -> Unit,
    notificationPermissionGranted: Boolean,
) {
    var page = androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (page.intValue) {
                0 -> WelcomePage()
                1 -> OratorPage(
                    state = state,
                    onSelectOrator = onSelectOrator,
                    onRotateAll = onRotateAll,
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
            if (page.intValue > 0) {
                OutlinedButton(
                    onClick = { page.intValue -= 1 },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.onboarding_back))
                }
            }
            Button(
                onClick = {
                    if (page.intValue < 2) page.intValue += 1 else onFinish()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (page.intValue < 2) {
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
        modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun OratorPage(
    state: OnboardingUiState,
    onSelectOrator: (Long) -> Unit,
    onRotateAll: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.onboarding_orator_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_orator_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FilterChip(
            selected = state.rotateThroughAll || state.selectedOratorId == null,
            onClick = { onRotateAll(true) },
            label = { Text(stringResource(R.string.profile_rotate_all)) },
        )
        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.orators, key = { it.id }) { orator ->
                Card(
                    onClick = { onSelectOrator(orator.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (orator.id == state.selectedOratorId && !state.rotateThroughAll) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OratorPortrait(
                            oratorId = orator.id,
                            oratorName = null,
                            size = 48.dp,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = orator.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = orator.era,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onToggleNotifications(granted)
    }

    androidx.compose.runtime.LaunchedEffect(state.notificationsEnabled) {
        if (state.notificationsEnabled &&
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                        !notificationPermissionGranted &&
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS,
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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
