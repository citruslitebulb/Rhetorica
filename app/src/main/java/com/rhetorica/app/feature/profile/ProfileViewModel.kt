package com.rhetorica.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        dictionaryRepository.observeActiveOratorProfiles(),
        userPreferencesDao.observeUserPreferences(),
    ) { orators, preferences ->
        ProfileUiState(
            orators = orators,
            selectedOratorId = preferences?.selectedOratorId,
            rotateThroughAll = preferences?.rotateThroughAll ?: false,
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
        }
    }
}

data class ProfileUiState(
    val orators: List<OratorProfile> = emptyList(),
    val selectedOratorId: Long? = null,
    val rotateThroughAll: Boolean = false,
    val isLoading: Boolean = false,
)
