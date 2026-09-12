package com.rhetorica.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.model.WordThemes
import com.rhetorica.app.data.repository.ProgressSnapshot
import com.rhetorica.app.ui.theme.RhetoricaGold
import com.rhetorica.app.widget.WidgetAppearance

@Composable
fun ProfileRoute(
    onPrivacyPolicy: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onPrivacyPolicy = onPrivacyPolicy,
        onSelectOrator = viewModel::selectOrator,
        onToggleFavorite = viewModel::toggleFavoriteOrator,
        onToggleRotateAll = viewModel::toggleRotateThroughAll,
        onToggleThemeCategory = viewModel::toggleThemeCategory,
        onClearThemeCategories = viewModel::clearThemeCategories,
        onSelectWidgetColor = viewModel::updateWidgetBackgroundColor,
        onWidgetOpacityChanged = viewModel::updateWidgetBackgroundOpacity,
        onSelectWidgetImage = viewModel::updateWidgetImagePreset,
        onGalleryUri = viewModel::updateWidgetGalleryUri,
        onNotificationsEnabled = viewModel::setNotificationsEnabled,
        onNotificationTime = viewModel::setNotificationTime,
        onThemeMode = viewModel::setThemeMode,
        onIncludeFictional = viewModel::setIncludeFictionalOrators,
        onIncludeLiterary = viewModel::setIncludeLiteraryOrators,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    state: ProfileUiState,
    onPrivacyPolicy: () -> Unit,
    onSelectOrator: (Long?) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onToggleRotateAll: () -> Unit,
    onToggleThemeCategory: (String) -> Unit,
    onClearThemeCategories: () -> Unit,
    onSelectWidgetColor: (Int) -> Unit,
    onWidgetOpacityChanged: (Int) -> Unit,
    onSelectWidgetImage: (com.rhetorica.app.widget.WidgetImagePreset) -> Unit,
    onGalleryUri: (String) -> Unit,
    onNotificationsEnabled: (Boolean) -> Unit,
    onNotificationTime: (Int, Int) -> Unit,
    onThemeMode: (com.rhetorica.app.core.model.ThemeMode) -> Unit,
    onIncludeFictional: (Boolean) -> Unit,
    onIncludeLiterary: (Boolean) -> Unit,
) {
    var widgetExpanded by rememberSaveable { mutableStateOf(false) }
    var themesExpanded by rememberSaveable { mutableStateOf(false) }
    var oratorsExpanded by rememberSaveable { mutableStateOf(false) }
    var habitExpanded by rememberSaveable { mutableStateOf(false) }
    var appearanceExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.nav_profile)) },
            )
        },
    ) { innerPadding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_progress_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            ProgressStatsRow(
                progress = state.progress,
                openedToday = state.openedTodaysWord,
            )
        }

        item {
            CollapsibleSectionHeader(
                title = stringResource(R.string.profile_habit_title),
                expanded = habitExpanded,
                onToggle = { habitExpanded = !habitExpanded },
            )
        }
        item {
            AnimatedVisibility(
                visible = habitExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                NotificationsCard(
                    enabled = state.notificationsEnabled,
                    hour = state.notificationHour,
                    minute = state.notificationMinute,
                    onEnabled = onNotificationsEnabled,
                    onTime = onNotificationTime,
                )
            }
        }

        item {
            CollapsibleSectionHeader(
                title = stringResource(R.string.profile_appearance_title),
                expanded = appearanceExpanded,
                onToggle = { appearanceExpanded = !appearanceExpanded },
            )
        }
        item {
            AnimatedVisibility(
                visible = appearanceExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ThemeModeCard(
                    selected = state.themeMode,
                    onSelect = onThemeMode,
                )
            }
        }

        item {
            CollapsibleSectionHeader(
                title = stringResource(R.string.profile_widget_appearance),
                expanded = widgetExpanded,
                onToggle = { widgetExpanded = !widgetExpanded },
            )
        }

        item {
            AnimatedVisibility(
                visible = widgetExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                WidgetAppearanceCard(
                    selectedColor = state.widgetBackgroundColor,
                    opacityPercent = state.widgetBackgroundOpacityPercent,
                    imagePreset = state.widgetImagePreset,
                    onSelectColor = onSelectWidgetColor,
                    onOpacityChanged = onWidgetOpacityChanged,
                    onSelectImage = onSelectWidgetImage,
                    onGalleryUri = onGalleryUri,
                )
            }
        }

        item {
            CollapsibleSectionHeader(
                title = stringResource(R.string.profile_themes_title),
                expanded = themesExpanded,
                onToggle = { themesExpanded = !themesExpanded },
            )
        }

        item {
            AnimatedVisibility(
                visible = themesExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_themes_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Wrap chips into rows so nothing scrolls off-screen horizontally.
                    // Extra rows grow downward; the parent LazyColumn handles vertical scroll.
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = state.selectedThemeCategories.isEmpty(),
                            onClick = { onClearThemeCategories() },
                            label = { Text(text = stringResource(R.string.saved_themes_all)) },
                        )
                        WordThemes.canonicalList().forEach { theme ->
                            FilterChip(
                                selected = theme in state.selectedThemeCategories,
                                onClick = { onToggleThemeCategory(theme) },
                                label = { Text(text = WordThemes.displayName(theme)) },
                            )
                        }
                    }
                }
            }
        }

        item {
            CollapsibleSectionHeader(
                title = stringResource(R.string.profile_orator_selection),
                expanded = oratorsExpanded,
                onToggle = { oratorsExpanded = !oratorsExpanded },
            )
        }

        item {
            AnimatedVisibility(
                visible = oratorsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CatalogTogglesCard(
                        includeLiterary = state.includeLiteraryOrators,
                        includeFictional = state.includeFictionalOrators,
                        onIncludeLiterary = onIncludeLiterary,
                        onIncludeFictional = onIncludeFictional,
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.profile_rotate_all),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = stringResource(R.string.profile_rotate_all_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = state.rotateThroughAll,
                                onCheckedChange = { onToggleRotateAll() },
                            )
                        }
                    }

                    if (state.orators.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Text(
                                text = if (state.selectedThemeCategories.isNotEmpty()) {
                                    stringResource(R.string.profile_no_orators_for_themes)
                                } else {
                                    stringResource(R.string.profile_no_orators)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    } else {
                        state.orators.forEach { orator ->
                            OratorCard(
                                orator = orator,
                                isSelected = state.selectedOratorId == orator.id && !state.rotateThroughAll,
                                isFavorite = orator.id in state.favoriteOratorIds,
                                onClick = { onSelectOrator(if (state.selectedOratorId == orator.id) null else orator.id) },
                                onToggleFavorite = { onToggleFavorite(orator.id) },
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.privacy_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onPrivacyPolicy)
                    .padding(vertical = 8.dp),
            )
        }
    }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WidgetAppearanceCard(
    selectedColor: Int,
    opacityPercent: Int,
    imagePreset: com.rhetorica.app.widget.WidgetImagePreset,
    onSelectColor: (Int) -> Unit,
    onOpacityChanged: (Int) -> Unit,
    onSelectImage: (com.rhetorica.app.widget.WidgetImagePreset) -> Unit,
    onGalleryUri: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // Some providers do not support persistable grants; still try the URI.
            }
            onGalleryUri(uri.toString())
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.profile_widget_color),
                    style = MaterialTheme.typography.bodyLarge,
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(WidgetAppearance.colorPresets, key = { it.colorValue }) { preset ->
                        WidgetColorSwatch(
                            colorValue = preset.colorValue,
                            label = stringResource(preset.labelRes),
                            isSelected = selectedColor == preset.colorValue,
                            onClick = { onSelectColor(preset.colorValue) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.profile_widget_image),
                    style = MaterialTheme.typography.bodyLarge,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    com.rhetorica.app.widget.WidgetImagePreset.entries
                        .filter { it != com.rhetorica.app.widget.WidgetImagePreset.Gallery }
                        .forEach { preset ->
                            FilterChip(
                                selected = imagePreset == preset,
                                onClick = { onSelectImage(preset) },
                                label = { Text(stringResource(preset.labelRes)) },
                            )
                        }
                    FilterChip(
                        selected = imagePreset == com.rhetorica.app.widget.WidgetImagePreset.Gallery,
                        onClick = { galleryLauncher.launch(arrayOf("image/*")) },
                        label = { Text(stringResource(R.string.widget_image_gallery)) },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_widget_opacity),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.profile_widget_opacity_value, opacityPercent),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Slider(
                    value = opacityPercent.toFloat(),
                    onValueChange = { onOpacityChanged(it.toInt()) },
                    valueRange = 0f..100f,
                )
            }

            WidgetPreview(
                colorValue = selectedColor,
                opacityPercent = opacityPercent,
            )
        }
    }
}

@Composable
private fun WidgetColorSwatch(
    colorValue: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(colorValue))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    },
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WidgetPreview(
    colorValue: Int,
    opacityPercent: Int,
) {
    // Mirrors the live widget: gold border over the user's fill color + opacity.
    val borderColor = RhetoricaGold
    val cardBg = WidgetAppearance.composeColor(colorValue, opacityPercent)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.widget_default_word),
                style = MaterialTheme.typography.titleLarge,
                color = borderColor,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.widget_default_definition),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFF5F0E6),
            )

            // Thin gold divider hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(1.dp)
                    .background(borderColor.copy(alpha = 0.7f)),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Laurel hint (simple circle + text as placeholder)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(borderColor.copy(alpha = 0.85f)),
                )
                Text(
                    text = stringResource(R.string.profile_widget_preview_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE8DFC8),
                )
            }
        }
    }
}

@Composable
private fun OratorCard(
    orator: com.rhetorica.app.core.model.OratorProfile,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            com.rhetorica.app.core.ui.OratorPortrait(
                oratorId = orator.id,
                oratorName = orator.name,
                size = 64.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = orator.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = orator.era,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = orator.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Filled.Bookmark
                    } else {
                        Icons.Outlined.BookmarkBorder
                    },
                    contentDescription = stringResource(
                        if (isFavorite) R.string.profile_unfavorite_orator else R.string.profile_favorite_orator,
                    ),
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ProgressStatsRow(
    progress: ProgressSnapshot,
    openedToday: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (openedToday) {
            Text(
                text = stringResource(R.string.profile_today_done),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgressStatCard(
                label = stringResource(R.string.profile_progress_streak),
                value = progress.dailyStreak.toString(),
                modifier = Modifier.weight(1f),
            )
            ProgressStatCard(
                label = stringResource(R.string.profile_progress_viewed),
                value = progress.uniqueWordsOpened.toString(),
                modifier = Modifier.weight(1f),
            )
            ProgressStatCard(
                label = stringResource(R.string.profile_progress_saved),
                value = progress.savedCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgressStatCard(
                label = stringResource(R.string.profile_progress_quiz),
                value = progress.quizAttemptCount.toString(),
                modifier = Modifier.weight(1f),
            )
            ProgressStatCard(
                label = stringResource(R.string.profile_progress_mastered),
                value = progress.masteredCount.toString(),
                modifier = Modifier.weight(1f),
            )
            ProgressStatCard(
                label = stringResource(R.string.profile_progress_due),
                value = progress.dueCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        if (progress.bestDailyStreak > 1) {
            Text(
                text = stringResource(R.string.profile_progress_best_streak, progress.bestDailyStreak),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProgressStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CollapsibleSectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "sectionChevron",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = stringResource(
                if (expanded) R.string.section_collapse else R.string.section_expand,
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabled: (Boolean) -> Unit,
    onTime: (Int, Int) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onEnabled(granted)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                        text = stringResource(R.string.profile_notifications_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { checked ->
                        if (checked &&
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS,
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onEnabled(checked)
                        }
                    },
                )
            }
            androidx.compose.material3.TextButton(onClick = { showPicker = true }) {
                Text(
                    text = stringResource(R.string.profile_notification_time_value, hour, minute),
                )
            }
            if (!enabled) {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                        ).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                ) {
                    Text(text = stringResource(R.string.profile_notification_system_settings))
                }
            }
        }
    }

    if (showPicker) {
        val pickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = false,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        onTime(pickerState.hour, pickerState.minute)
                        showPicker = false
                    },
                ) {
                    Text(text = stringResource(R.string.onboarding_done))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPicker = false }) {
                    Text(text = stringResource(R.string.back))
                }
            },
            text = {
                androidx.compose.material3.TimePicker(state = pickerState)
            },
        )
    }
}

@Composable
private fun ThemeModeCard(
    selected: com.rhetorica.app.core.model.ThemeMode,
    onSelect: (com.rhetorica.app.core.model.ThemeMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_theme_title),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selected == com.rhetorica.app.core.model.ThemeMode.System,
                    onClick = { onSelect(com.rhetorica.app.core.model.ThemeMode.System) },
                    label = { Text(stringResource(R.string.profile_theme_system)) },
                )
                FilterChip(
                    selected = selected == com.rhetorica.app.core.model.ThemeMode.Dark,
                    onClick = { onSelect(com.rhetorica.app.core.model.ThemeMode.Dark) },
                    label = { Text(stringResource(R.string.profile_theme_dark)) },
                )
                FilterChip(
                    selected = selected == com.rhetorica.app.core.model.ThemeMode.Light,
                    onClick = { onSelect(com.rhetorica.app.core.model.ThemeMode.Light) },
                    label = { Text(stringResource(R.string.profile_theme_light)) },
                )
            }
        }
    }
}

@Composable
private fun CatalogTogglesCard(
    includeLiterary: Boolean,
    includeFictional: Boolean,
    onIncludeLiterary: (Boolean) -> Unit,
    onIncludeFictional: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_catalog_title),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.profile_catalog_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.profile_catalog_literary))
                Switch(checked = includeLiterary, onCheckedChange = onIncludeLiterary)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(R.string.profile_catalog_fictional))
                Switch(checked = includeFictional, onCheckedChange = onIncludeFictional)
            }
        }
    }
}
