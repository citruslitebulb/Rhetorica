package com.rhetorica.app.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.HabitProgress
import com.rhetorica.app.core.model.OratorCatalogKind.Companion.filterByCatalog
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.core.model.ThemeMode
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.PreferencesRepository
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.data.repository.ProgressSnapshot
import com.rhetorica.app.data.repository.WordRepository
import com.rhetorica.app.notification.NotificationScheduler
import com.rhetorica.app.widget.WidgetAppearance
import com.rhetorica.app.widget.WidgetImagePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProfileUiState> = preferencesRepository.observe().flatMapLatest { preferences ->
        combine(
            dictionaryRepository.observeActiveOratorProfiles(),
            progressRepository.observeProgressSnapshot(),
            progressRepository.observeIsWordOpened(preferences.todaysWotdId ?: -1L),
        ) { orators, progress, openedTodaysWord ->
            val visibleOrators = orators.filterByCatalog(
                includeLiterary = preferences.includeLiteraryOrators,
                includeFictional = preferences.includeFictionalOrators,
            )
            val selectedThemes = preferences.selectedThemeCategories
            val filteredOrators = if (selectedThemes.isEmpty()) {
                visibleOrators
            } else {
                visibleOrators.filter { orator ->
                    orator.themeCategories.any { theme -> theme in selectedThemes }
                }
            }
            val effectiveSelectedOratorId = preferences.selectedOratorId?.takeIf { id ->
                filteredOrators.any { it.id == id }
            }
            ProfileUiState(
                orators = filteredOrators,
                selectedOratorId = effectiveSelectedOratorId,
                favoriteOratorIds = preferences.favoriteOratorIds.toSet(),
                rotateThroughAll = preferences.rotateThroughAll,
                selectedThemeCategories = selectedThemes,
                widgetBackgroundColor = preferences.widgetBackgroundColor,
                widgetBackgroundOpacityPercent = preferences.widgetBackgroundOpacityPercent,
                widgetImagePreset = WidgetImagePreset.fromKey(preferences.widgetBackgroundImageKey),
                widgetGalleryUri = preferences.widgetGalleryUri,
                notificationsEnabled = preferences.notificationsEnabled,
                notificationHour = preferences.notificationHour,
                notificationMinute = preferences.notificationMinute,
                themeMode = ThemeMode.fromStorage(preferences.themeMode),
                includeFictionalOrators = preferences.includeFictionalOrators,
                includeLiteraryOrators = preferences.includeLiteraryOrators,
                openedTodaysWord = HabitProgress.isTodaysWordOpened(
                    todaysWotdId = preferences.todaysWotdId,
                    todaysWotdDate = preferences.todaysWotdDate,
                    isOpened = openedTodaysWord,
                ),
                progress = progress,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    init {
        viewModelScope.launch {
            progressRepository.syncSavedCount()
        }
    }

    fun selectOrator(oratorId: Long?) {
        viewModelScope.launch {
            updatePoolAndRefresh { current ->
                current.copy(
                    selectedOratorId = oratorId,
                    rotateThroughAll = if (oratorId != null) false else current.rotateThroughAll,
                )
            }
        }
    }

    fun toggleFavoriteOrator(oratorId: Long) {
        viewModelScope.launch {
            updatePoolAndRefresh { current ->
                val next = if (oratorId in current.favoriteOratorIds) {
                    current.favoriteOratorIds - oratorId
                } else {
                    current.favoriteOratorIds + oratorId
                }
                current.copy(favoriteOratorIds = next)
            }
        }
    }

    fun toggleRotateThroughAll() {
        viewModelScope.launch {
            updatePoolAndRefresh { current ->
                val enableRotate = !current.rotateThroughAll
                current.copy(
                    rotateThroughAll = enableRotate,
                    selectedOratorId = if (enableRotate) null else current.selectedOratorId,
                )
            }
        }
    }

    fun toggleThemeCategory(category: String) {
        viewModelScope.launch {
            preferencesRepository.update { current ->
                val updatedThemes = if (category in current.selectedThemeCategories) {
                    current.selectedThemeCategories - category
                } else {
                    current.selectedThemeCategories + category
                }
                current.copy(selectedThemeCategories = updatedThemes)
            }
        }
    }

    fun clearThemeCategories() {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(selectedThemeCategories = emptyList()) }
        }
    }

    fun updateWidgetBackgroundColor(colorValue: Int) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(
                    widgetBackgroundColor = colorValue,
                    widgetBackgroundImageKey = WidgetImagePreset.None.key,
                )
            }
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun updateWidgetBackgroundOpacity(opacityPercent: Int) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(widgetBackgroundOpacityPercent = opacityPercent.coerceIn(0, 100))
            }
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun updateWidgetImagePreset(preset: WidgetImagePreset) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(widgetBackgroundImageKey = preset.key)
            }
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun updateWidgetGalleryUri(uri: String) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(
                    widgetGalleryUri = uri,
                    widgetBackgroundImageKey = WidgetImagePreset.Gallery.key,
                )
            }
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(notificationsEnabled = enabled) }
            val prefs = preferencesRepository.get()
            NotificationScheduler.reschedule(
                context = context,
                enabled = prefs.notificationsEnabled,
                hour = prefs.notificationHour,
                minute = prefs.notificationMinute,
            )
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(
                    notificationHour = hour.coerceIn(0, 23),
                    notificationMinute = minute.coerceIn(0, 59),
                )
            }
            val prefs = preferencesRepository.get()
            NotificationScheduler.reschedule(
                context = context,
                enabled = prefs.notificationsEnabled,
                hour = prefs.notificationHour,
                minute = prefs.notificationMinute,
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(themeMode = mode.storageValue) }
        }
    }

    fun setIncludeFictionalOrators(include: Boolean) {
        viewModelScope.launch {
            updatePoolAndRefresh { it.copy(includeFictionalOrators = include) }
        }
    }

    fun setIncludeLiteraryOrators(include: Boolean) {
        viewModelScope.launch {
            updatePoolAndRefresh { it.copy(includeLiteraryOrators = include) }
        }
    }

    private suspend fun updatePoolAndRefresh(
        transform: (UserPreferencesEntity) -> UserPreferencesEntity,
    ) {
        preferencesRepository.update(transform)
        wordRepository.ensureTodaysWord()
        WidgetAppearance.refreshAllWidgets(context)
    }
}

data class ProfileUiState(
    val orators: List<OratorProfile> = emptyList(),
    val selectedOratorId: Long? = null,
    val favoriteOratorIds: Set<Long> = emptySet(),
    val rotateThroughAll: Boolean = false,
    val selectedThemeCategories: List<String> = emptyList(),
    val widgetBackgroundColor: Int = 0xFF2C3E50.toInt(),
    val widgetBackgroundOpacityPercent: Int = 80,
    val widgetImagePreset: WidgetImagePreset = WidgetImagePreset.None,
    val widgetGalleryUri: String = "",
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val themeMode: ThemeMode = ThemeMode.Dark,
    val includeFictionalOrators: Boolean = false,
    val includeLiteraryOrators: Boolean = false,
    val openedTodaysWord: Boolean = false,
    val progress: ProgressSnapshot = ProgressSnapshot(0, 0, 0, 0, 0, 0),
    val isLoading: Boolean = false,
)
