package com.rhetorica.app.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.core.model.OratorVoiceFamily
import com.rhetorica.app.core.model.ThemeMode
import com.rhetorica.app.core.model.WordThemes
import com.rhetorica.app.ui.theme.RhetoricaGold
import com.rhetorica.app.ui.theme.RhetoricaTheme

private const val PAGE_WELCOME = 0
private const val PAGE_INTERESTS = 1
private const val PAGE_HABIT = 2
private const val PAGE_COUNT = 3

private val OnboardingGold = RhetoricaGold
private val OnboardingGoldSoft = Color(0xFFFFEBA4)
private val OnboardingSelected = Color(0xFF4A3B18)
private val OnboardingCard = Color(0xFF2B2B2B)
private val OnboardingTrack = Color(0xFF2F2F2F)
private val OnboardingMuted = Color(0xFFB3B3B3)
private val OnboardingInk = Color(0xFF111111)
private val OnboardingChipBorder = Color(0xFF5A5A5A)
private val PillShape = RoundedCornerShape(percent = 50)
private val CardShape = RoundedCornerShape(12.dp)

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RhetoricaTheme(themeMode = ThemeMode.Dark) {
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
                color = OnboardingMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            StepProgress(fraction = (page + 1) / PAGE_COUNT.toFloat())
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
                OnboardingOutlinedButton(
                    text = stringResource(R.string.onboarding_back),
                    onClick = { page -= 1 },
                    modifier = Modifier.weight(1f),
                )
            }
            OnboardingFilledButton(
                text = if (page < PAGE_HABIT) {
                    stringResource(R.string.onboarding_next)
                } else {
                    stringResource(R.string.onboarding_done)
                },
                onClick = {
                    if (page < PAGE_HABIT) page += 1 else onFinish()
                },
                enabled = canAdvance,
                modifier = if (page > PAGE_WELCOME) Modifier.weight(1f) else Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StepProgress(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(OnboardingTrack),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0.08f, 1f))
                .fillMaxHeight()
                .background(OnboardingGold),
        )
    }
}

@Composable
private fun OnboardingFilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = OnboardingGold,
            contentColor = OnboardingInk,
            disabledContainerColor = OnboardingGold.copy(alpha = 0.38f),
            disabledContentColor = OnboardingInk.copy(alpha = 0.5f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun OnboardingOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = OnboardingGold,
        ),
        border = BorderStroke(1.dp, OnboardingGold),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ColumnMark()
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(top = 12.dp),
            color = OnboardingGold,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.app_tagline),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            color = Color(0xFFEAEAEA),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_body),
            color = OnboardingMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_questions),
            modifier = Modifier.padding(top = 12.dp),
            color = OnboardingMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ColumnMark(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(width = 56.dp, height = 72.dp),
    ) {
        val sx = size.width / 64f
        val sy = size.height / 96f
        val gold = OnboardingGold
        val mid = Color(0xFFC9A44A)
        val light = Color(0xFFE0C36A)
        val dark = Color(0xFFB48B3A)
        drawRoundRect(
            color = gold,
            topLeft = Offset(14f * sx, 8f * sy),
            size = Size(36f * sx, 8f * sy),
            cornerRadius = CornerRadius(1.5f * sx, 1.5f * sy),
        )
        drawRoundRect(
            color = mid,
            topLeft = Offset(18f * sx, 16f * sy),
            size = Size(28f * sx, 6f * sy),
            cornerRadius = CornerRadius(sx, sy),
        )
        drawRect(color = mid, topLeft = Offset(22f * sx, 24f * sy), size = Size(4f * sx, 50f * sy))
        drawRect(color = light, topLeft = Offset(28f * sx, 24f * sy), size = Size(3.5f * sx, 50f * sy))
        drawRect(color = mid, topLeft = Offset(34f * sx, 24f * sy), size = Size(3.5f * sx, 50f * sy))
        drawRect(color = dark, topLeft = Offset(40f * sx, 24f * sy), size = Size(4f * sx, 50f * sy))
        drawRoundRect(
            color = mid,
            topLeft = Offset(18f * sx, 74f * sy),
            size = Size(28f * sx, 6f * sy),
            cornerRadius = CornerRadius(sx, sy),
        )
        drawRoundRect(
            color = gold,
            topLeft = Offset(12f * sx, 82f * sy),
            size = Size(40f * sx, 8f * sy),
            cornerRadius = CornerRadius(1.5f * sx, 1.5f * sy),
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
            CircularProgressIndicator(color = OnboardingGold)
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
                    color = Color(0xFFEAEAEA),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                )
                Text(
                    text = stringResource(R.string.onboarding_voices_body),
                    color = OnboardingMuted,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
                if (state.selectedFamilies.isEmpty()) {
                    Text(
                        text = stringResource(R.string.onboarding_voices_need_one),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
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
                    color = Color(0xFFEAEAEA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.onboarding_themes_body),
                    color = OnboardingMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeChip(
                        label = stringResource(R.string.saved_themes_all),
                        selected = state.selectedThemes.isEmpty(),
                        onClick = onClearThemes,
                    )
                    WordThemes.canonicalList().forEach { theme ->
                        ThemeChip(
                            label = WordThemes.displayName(theme),
                            selected = theme in state.selectedThemes,
                            onClick = { onToggleTheme(theme) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) OnboardingSelected else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) OnboardingGold else OnboardingChipBorder,
                shape = shape,
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = if (selected) OnboardingGoldSoft else Color(0xFFEAEAEA),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun VoiceFamilyCard(
    family: OratorVoiceFamily,
    selected: Boolean,
    members: List<OratorProfile>,
    onToggle: () -> Unit,
) {
    val bodyColor = if (selected) OnboardingGoldSoft.copy(alpha = 0.85f) else OnboardingMuted
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(if (selected) OnboardingSelected else OnboardingCard)
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(familyTitleRes(family)),
                    color = Color(0xFFEAEAEA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(familyBodyRes(family)),
                    color = bodyColor,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
                if (members.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.onboarding_family_count, members.size),
                        color = OnboardingGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = OnboardingGold,
                    modifier = Modifier.size(20.dp),
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
            color = Color(0xFFEAEAEA),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_habit_body),
            color = OnboardingMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_notifications_title),
                    color = Color(0xFFEAEAEA),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(
                        R.string.profile_notification_time_value,
                        state.notificationHour,
                        state.notificationMinute,
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    color = OnboardingMuted,
                    fontSize = 14.sp,
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
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OnboardingInk,
                    checkedTrackColor = OnboardingGold,
                    checkedBorderColor = OnboardingGold,
                    uncheckedThumbColor = Color(0xFFCAC4D0),
                    uncheckedTrackColor = OnboardingCard,
                    uncheckedBorderColor = OnboardingChipBorder,
                ),
            )
        }
        if (state.canPinWidget) {
            OnboardingOutlinedButton(
                text = stringResource(R.string.onboarding_add_widget),
                onClick = onPinWidget,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = stringResource(R.string.onboarding_widget_hint),
                color = OnboardingMuted,
                fontSize = 13.sp,
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
