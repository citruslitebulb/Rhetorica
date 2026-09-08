package com.rhetorica.app.feature.onboarding

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhetorica.app.core.model.OnboardingAnswers
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.core.model.OratorVoiceFamily
import com.rhetorica.app.core.model.OratorVoiceFamily.Companion.filterByFamilies
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.PreferencesRepository
import com.rhetorica.app.data.repository.WordRepository
import com.rhetorica.app.notification.NotificationScheduler
import com.rhetorica.app.widget.WidgetAppearance
import com.rhetorica.app.widget.WordOfDayWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    dictionaryRepository: DictionaryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val wordRepository: WordRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val selectedFamilies = MutableStateFlow(OratorVoiceFamily.defaultSelected)
    private val selectedThemes = MutableStateFlow<Set<String>>(emptySet())
    private val notificationsEnabled = MutableStateFlow(true)

    val uiState: StateFlow<OnboardingUiState> = combine(
        dictionaryRepository.observeActiveOratorProfiles(),
        selectedFamilies,
        selectedThemes,
        notificationsEnabled,
    ) { orators, families, themes, notifications ->
        val matchingOrators = orators.filterByFamilies(families)
        OnboardingUiState(
            orators = orators,
            selectedFamilies = families,
            selectedThemes = themes,
            notificationsEnabled = notifications,
            canPinWidget = AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported,
            canContinueFromVoices = families.isNotEmpty() && matchingOrators.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState(),
    )

    fun toggleFamily(family: OratorVoiceFamily) {
        selectedFamilies.update { current ->
            if (family in current) current - family else current + family
        }
    }

    fun toggleTheme(theme: String) {
        selectedThemes.update { current ->
            if (theme in current) current - theme else current + theme
        }
    }

    fun clearThemes() {
        selectedThemes.value = emptySet()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
    }

    fun requestPinWidget() {
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return
        val provider = ComponentName(context, WordOfDayWidgetProvider::class.java)
        manager.requestPinAppWidget(provider, null, null)
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            val state = uiState.value
            val granted = notificationPermissionGranted()
            val patch = OnboardingAnswers.resolve(
                orators = state.orators,
                families = state.selectedFamilies,
                themes = state.selectedThemes,
                selectedOratorIds = emptySet(),
            )
            preferencesRepository.update { current ->
                current.copy(
                    onboardingCompleted = true,
                    favoriteOratorIds = patch.favoriteOratorIds,
                    selectedOratorId = patch.selectedOratorId,
                    rotateThroughAll = patch.rotateThroughAll,
                    selectedThemeCategories = patch.selectedThemeCategories,
                    includeLiteraryOrators = patch.includeLiteraryOrators,
                    includeFictionalOrators = patch.includeFictionalOrators,
                    notificationsEnabled = state.notificationsEnabled && granted,
                    todaysWotdDate = "",
                    todaysWotdId = null,
                )
            }
            val prefs = preferencesRepository.get()
            NotificationScheduler.reschedule(
                context = context,
                enabled = prefs.notificationsEnabled,
                hour = prefs.notificationHour,
                minute = prefs.notificationMinute,
            )
            wordRepository.ensureTodaysWord()
            WidgetAppearance.refreshAllWidgets(context)
            onDone()
        }
    }

    fun notificationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}

data class OnboardingUiState(
    val orators: List<OratorProfile> = emptyList(),
    val selectedFamilies: Set<OratorVoiceFamily> = OratorVoiceFamily.defaultSelected,
    val selectedThemes: Set<String> = emptySet(),
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val canPinWidget: Boolean = false,
    val canContinueFromVoices: Boolean = false,
)
