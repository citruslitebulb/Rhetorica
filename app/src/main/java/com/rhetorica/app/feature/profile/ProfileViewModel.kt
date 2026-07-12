package com.rhetorica.app.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.ProgressRepository
import com.rhetorica.app.widget.WidgetAppearance
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val userPreferencesDao: UserPreferencesDao,
    private val progressRepository: ProgressRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        dictionaryRepository.observeActiveOratorProfiles(),
        userPreferencesDao.observeUserPreferences(),
        progressRepository.observeProgress(),
    ) { orators, preferences, progress ->
        val selectedThemes = preferences?.selectedThemeCategories ?: emptyList()
        val filteredOrators = if (selectedThemes.isEmpty()) {
            orators
        } else {
            orators.filter { orator ->
                orator.themeCategories.any { theme -> theme in selectedThemes }
            }
        }
        val effectiveSelectedOratorId = preferences?.selectedOratorId?.takeIf { id ->
            filteredOrators.any { it.id == id }
        }
        ProfileUiState(
            orators = filteredOrators,
            selectedOratorId = effectiveSelectedOratorId,
            rotateThroughAll = preferences?.rotateThroughAll ?: false,
            selectedThemeCategories = selectedThemes,
            widgetBackgroundColor = preferences?.widgetBackgroundColor ?: 0xFF2C3E50.toInt(),
            widgetBackgroundOpacityPercent = preferences?.widgetBackgroundOpacityPercent ?: 80,
            viewedCount = progress?.viewedCount ?: 0,
            savedCount = progress?.savedCount ?: 0,
            quizCorrectCount = progress?.quizCorrectCount ?: 0,
        )
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
            val currentPrefs = userPreferencesDao.getUserPreferences()
            // Selecting a specific orator must exit "rotate through all" so Word of the Day
            // is taken from that orator's vocabulary (not a global word re-labeled).
            val updatedPrefs = currentPrefs?.copy(
                selectedOratorId = oratorId,
                rotateThroughAll = if (oratorId != null) false else currentPrefs.rotateThroughAll,
            ) ?: UserPreferencesEntity(
                favoriteOratorIds = emptyList(),
                rotateThroughAll = false,
                selectedOratorId = oratorId,
            )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun toggleRotateThroughAll() {
        viewModelScope.launch {
            val currentPrefs = userPreferencesDao.getUserPreferences()
            val enableRotate = !(currentPrefs?.rotateThroughAll ?: false)
            val updatedPrefs = currentPrefs?.copy(
                rotateThroughAll = enableRotate,
                // Global rotation means no single orator owns the daily word.
                selectedOratorId = if (enableRotate) null else currentPrefs.selectedOratorId,
            ) ?: UserPreferencesEntity(
                favoriteOratorIds = emptyList(),
                rotateThroughAll = true,
                selectedOratorId = null,
            )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun toggleThemeCategory(category: String) {
        viewModelScope.launch {
            val currentPrefs = userPreferencesDao.getUserPreferences()
            val currentThemes = currentPrefs?.selectedThemeCategories ?: emptyList()
            val updatedThemes = if (category in currentThemes) {
                currentThemes - category
            } else {
                currentThemes + category
            }
            val updatedPrefs = currentPrefs?.copy(selectedThemeCategories = updatedThemes)
                ?: UserPreferencesEntity(
                    favoriteOratorIds = emptyList(),
                    rotateThroughAll = false,
                    selectedOratorId = null,
                    selectedThemeCategories = updatedThemes,
                )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
        }
    }

    fun clearThemeCategories() {
        viewModelScope.launch {
            val currentPrefs = userPreferencesDao.getUserPreferences()
            val updatedPrefs = currentPrefs?.copy(selectedThemeCategories = emptyList())
                ?: UserPreferencesEntity(
                    favoriteOratorIds = emptyList(),
                    rotateThroughAll = false,
                    selectedOratorId = null,
                    selectedThemeCategories = emptyList(),
                )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
        }
    }

    fun updateWidgetBackgroundColor(colorValue: Int) {
        viewModelScope.launch {
            val currentPrefs = userPreferencesDao.getUserPreferences()
            val updatedPrefs = currentPrefs?.copy(widgetBackgroundColor = colorValue)
                ?: UserPreferencesEntity(
                    favoriteOratorIds = emptyList(),
                    rotateThroughAll = false,
                    selectedOratorId = null,
                    widgetBackgroundColor = colorValue,
                    widgetBackgroundOpacityPercent = 80,
                )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
            WidgetAppearance.refreshAllWidgets(context)
        }
    }

    fun updateWidgetBackgroundOpacity(opacityPercent: Int) {
        viewModelScope.launch {
            val currentPrefs = userPreferencesDao.getUserPreferences()
            val updatedPrefs = currentPrefs?.copy(
                widgetBackgroundOpacityPercent = opacityPercent.coerceIn(20, 100),
            ) ?: UserPreferencesEntity(
                favoriteOratorIds = emptyList(),
                rotateThroughAll = false,
                selectedOratorId = null,
                widgetBackgroundColor = 0xFF2C3E50.toInt(),
                widgetBackgroundOpacityPercent = opacityPercent.coerceIn(20, 100),
            )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
            WidgetAppearance.refreshAllWidgets(context)
        }
    }
}

data class ProfileUiState(
    val orators: List<OratorProfile> = emptyList(),
    val selectedOratorId: Long? = null,
    val rotateThroughAll: Boolean = false,
    val selectedThemeCategories: List<String> = emptyList(),
    val widgetBackgroundColor: Int = 0xFF2C3E50.toInt(),
    val widgetBackgroundOpacityPercent: Int = 80,
    val viewedCount: Int = 0,
    val savedCount: Int = 0,
    val quizCorrectCount: Int = 0,
    val isLoading: Boolean = false,
)
