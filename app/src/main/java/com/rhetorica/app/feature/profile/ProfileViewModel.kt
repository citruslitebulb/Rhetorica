package com.rhetorica.app.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.seed.SeedDataLoader
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
    private val seedDataLoader: SeedDataLoader,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        dictionaryRepository.observeActiveOratorProfiles(),
        userPreferencesDao.observeUserPreferences(),
    ) { orators, preferences ->
        ProfileUiState(
            orators = orators,
            selectedOratorId = preferences?.selectedOratorId,
            rotateThroughAll = preferences?.rotateThroughAll ?: false,
            widgetBackgroundColor = preferences?.widgetBackgroundColor ?: 0xFF2C3E50.toInt(),
            widgetBackgroundOpacityPercent = preferences?.widgetBackgroundOpacityPercent ?: 80,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun selectOrator(oratorId: Long?) {
        viewModelScope.launch {
            val currentPrefs = userPreferencesDao.getUserPreferences()
            val updatedPrefs = currentPrefs?.copy(selectedOratorId = oratorId)
                ?: UserPreferencesEntity(
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
            val updatedPrefs = currentPrefs?.copy(rotateThroughAll = !currentPrefs.rotateThroughAll)
                ?: UserPreferencesEntity(
                    favoriteOratorIds = emptyList(),
                    rotateThroughAll = true,
                    selectedOratorId = null,
                )
            userPreferencesDao.upsertUserPreferences(updatedPrefs)
            WidgetAppearance.refreshAllWidgets(context)
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

    fun reSeedData() {
        viewModelScope.launch {
            try {
                seedDataLoader.loadSeedDataIfNeeded()
                // Note: after this, user may need to navigate back to the word list or detail to see fresh data
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Re-seed failed", e)
            }
        }
    }
}

data class ProfileUiState(
    val orators: List<OratorProfile> = emptyList(),
    val selectedOratorId: Long? = null,
    val rotateThroughAll: Boolean = false,
    val widgetBackgroundColor: Int = 0xFF2C3E50.toInt(),
    val widgetBackgroundOpacityPercent: Int = 80,
    val isLoading: Boolean = false,
)
