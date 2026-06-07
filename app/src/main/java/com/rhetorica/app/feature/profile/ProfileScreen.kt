package com.rhetorica.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rhetorica.app.R
import com.rhetorica.app.core.model.WordThemes
import com.rhetorica.app.widget.WidgetAppearance

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onSelectOrator = viewModel::selectOrator,
        onToggleRotateAll = viewModel::toggleRotateThroughAll,
        onToggleThemeCategory = viewModel::toggleThemeCategory,
        onClearThemeCategories = viewModel::clearThemeCategories,
        onSelectWidgetColor = viewModel::updateWidgetBackgroundColor,
        onWidgetOpacityChanged = viewModel::updateWidgetBackgroundOpacity,
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileUiState,
    onSelectOrator: (Long?) -> Unit,
    onToggleRotateAll: () -> Unit,
    onToggleThemeCategory: (String) -> Unit,
    onClearThemeCategories: () -> Unit,
    onSelectWidgetColor: (Int) -> Unit,
    onWidgetOpacityChanged: (Int) -> Unit,
) {
    var widgetExpanded by rememberSaveable { mutableStateOf(false) }
    var themesExpanded by rememberSaveable { mutableStateOf(false) }
    var oratorsExpanded by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                    onSelectColor = onSelectWidgetColor,
                    onOpacityChanged = onWidgetOpacityChanged,
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.profile_themes_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = state.selectedThemeCategories.isEmpty(),
                                onClick = { onClearThemeCategories() },
                                label = { Text(text = stringResource(R.string.saved_themes_all)) },
                            )
                        }
                        val canonicalThemes = WordThemes.canonicalList()
                        items(canonicalThemes, key = { it }) { theme ->
                            val label = WordThemes.displayName(theme)
                            FilterChip(
                                selected = theme in state.selectedThemeCategories,
                                onClick = { onToggleThemeCategory(theme) },
                                label = { Text(text = label) },
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
                                    text = "Cycle through all orators daily",
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
                                onClick = { onSelectOrator(if (state.selectedOratorId == orator.id) null else orator.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetAppearanceCard(
    selectedColor: Int,
    opacityPercent: Int,
    onSelectColor: (Int) -> Unit,
    onOpacityChanged: (Int) -> Unit,
) {
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
                    valueRange = 20f..100f,
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
    val previewColor = WidgetAppearance.composeColor(colorValue, opacityPercent)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(previewColor)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.widget_default_word),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.widget_default_definition),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.92f),
            )
        }
    }
}

@Composable
private fun OratorCard(
    orator: com.rhetorica.app.core.model.OratorProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
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
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(
                    id = android.R.drawable.ic_menu_gallery,
                ),
                contentDescription = orator.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Color(
                            orator.colorAccent.toInt(),
                        ),
                    ),
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
            RadioButton(
                selected = isSelected,
                onClick = onClick,
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Collapse section" else "Expand section",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation),
        )
    }
}
