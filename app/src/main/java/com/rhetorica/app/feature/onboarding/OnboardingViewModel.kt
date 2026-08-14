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
import com.rhetorica.app.core.model.OratorCatalogKind.Companion.filterByCatalog
import com.rhetorica.app.core.model.OratorProfile
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.PreferencesRepository
import com.rhetorica.app.data.repository.WordRepository
import com.rhetorica.app.notification.NotificationScheduler
import com.rhetorica.app.widget.WidgetAppearance
import com.rhetorica.app.widget.WordOfDayWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val preferencesRepository: PreferencesRepository,
    private val wordRepository: WordRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val uiState: StateFlow<OnboardingUiState> = combine(
        dictionaryRepository.observeActiveOratorProfiles(),
        preferencesRepository.observe(),
    ) { orators, prefs ->
        val visible = orators.filterByCatalog(
            includeLiterary = prefs.includeLiteraryOrators,
            includeFictional = false,
        )
        OnboardingUiState(
            orators = visible,
            selectedOratorId = prefs.selectedOratorId,
            rotateThroughAll = prefs.rotateThroughAll,
            notificationsEnabled = prefs.notificationsEnabled,
            notificationHour = prefs.notificationHour,
            notificationMinute = prefs.notificationMinute,
            canPinWidget = AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState(),
    )

    fun selectOrator(oratorId: Long) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(
                    selectedOratorId = oratorId,
                    rotateThroughAll = false,
                    todaysWotdDate = "",
                    todaysWotdId = null,
                )
            }
        }
    }

    fun setRotateThroughAll(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update {
                it.copy(
                    rotateThroughAll = enabled,
                    selectedOratorId = if (enabled) null else it.selectedOratorId,
                    todaysWotdDate = "",
                    todaysWotdId = null,
                )
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.update { it.copy(notificationsEnabled = enabled) }
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
        }
    }

    fun requestPinWidget() {
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return
        val provider = ComponentName(context, WordOfDayWidgetProvider::class.java)
        manager.requestPinAppWidget(provider, null, null)
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            val granted = notificationPermissionGranted()
            preferencesRepository.update { current ->
                current.copy(
                    onboardingCompleted = true,
                    includeFictionalOrators = false,
                    notificationsEnabled = current.notificationsEnabled && granted,
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
    val selectedOratorId: Long? = null,
    val rotateThroughAll: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val canPinWidget: Boolean = false,
)
