package com.rhetorica.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rhetorica.app.core.model.ThemeMode
import com.rhetorica.app.core.navigation.OnboardingGate
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.UserPreferencesEntity
import com.rhetorica.app.data.repository.PreferencesRepository
import com.rhetorica.app.feature.onboarding.OnboardingRoute
import com.rhetorica.app.feature.speech.navigateToFullSpeech
import com.rhetorica.app.feature.word.navigateToWordDetail
import com.rhetorica.app.notification.NotificationPermissionGate
import com.rhetorica.app.notification.NotificationScheduler
import com.rhetorica.app.notification.WordNotificationHelper
import com.rhetorica.app.ui.RhetoricaApp
import com.rhetorica.app.ui.theme.RhetoricaTheme
import com.rhetorica.app.widget.WordOfDayWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferencesDao: UserPreferencesDao

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val incomingIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    private var askedNotificationPermission = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) return@registerForActivityResult
        lifecycleScope.launch {
            preferencesRepository.update { it.copy(notificationsEnabled = false) }
            val prefs = preferencesRepository.get()
            NotificationScheduler.reschedule(
                context = this@MainActivity,
                enabled = false,
                hour = prefs.notificationHour,
                minute = prefs.notificationMinute,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val prefsLoad by userPreferencesDao.observeUserPreferences()
                .map { PrefsLoad(ready = true, preferences = it) }
                .collectAsStateWithLifecycle(initialValue = PrefsLoad(ready = false, preferences = null))
            val themeMode = ThemeMode.fromStorage(prefsLoad.preferences?.themeMode)
            val needsOnboarding = OnboardingGate.needsOnboarding(prefsLoad.preferences)

            RhetoricaTheme(themeMode = themeMode) {
                if (!prefsLoad.ready) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (needsOnboarding) {
                    OnboardingRoute(onFinished = { })
                } else {
                    val navController = rememberNavController()
                    val launchIntent = remember { intent }

                    LaunchedEffect(navController) {
                        navController.currentBackStackEntryFlow.first()
                        handleNavigationIntent(launchIntent, navController)
                        incomingIntents.collect { navIntent ->
                            handleNavigationIntent(navIntent, navController)
                        }
                    }

                    LaunchedEffect(prefsLoad.preferences?.notificationsEnabled) {
                        maybeRequestNotificationPermission(prefsLoad.preferences)
                    }

                    RhetoricaApp(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingIntents.tryEmit(intent)
    }

    private fun maybeRequestNotificationPermission(preferences: UserPreferencesEntity?) {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (!NotificationPermissionGate.shouldRequest(
                onboardingCompleted = preferences?.onboardingCompleted == true,
                notificationsEnabled = preferences?.notificationsEnabled == true,
                sdkInt = Build.VERSION.SDK_INT,
                permissionGranted = granted,
                alreadyAskedThisProcess = askedNotificationPermission,
            )
        ) {
            return
        }
        askedNotificationPermission = true
        Handler(Looper.getMainLooper()).postDelayed({
            if (isDestroyed || isFinishing) return@postDelayed
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }, 400)
    }

    private fun handleNavigationIntent(intent: Intent?, navController: NavHostController) {
        if (intent == null) return

        if (intent.action == WordOfDayWidgetProvider.ACTION_OPEN_SPEECH_FROM_WIDGET) {
            val oratorId = intent.getLongExtra(WordOfDayWidgetProvider.EXTRA_ORATOR_ID, -1L)
            val speechTitle = intent.getStringExtra(WordOfDayWidgetProvider.EXTRA_SPEECH_TITLE)
            if (oratorId != -1L && !speechTitle.isNullOrBlank()) {
                navController.navigateToFullSpeech(oratorId, speechTitle)
                intent.action = null
                return
            }
        }

        if (intent.action == WordNotificationHelper.ACTION_MORE_INFO) {
            val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
            if (wordId != -1L) {
                navController.navigateToWordDetail(wordId)
                intent.action = null
                return
            }
        }

        intent.data?.let { uri ->
            if (uri.scheme == "rhetorica" && uri.host == "word") {
                uri.lastPathSegment?.toLongOrNull()?.let { wordId ->
                    navController.navigateToWordDetail(wordId)
                    intent.data = null
                    return
                }
            }
            if (uri.scheme == "rhetorica" && uri.host == "speech") {
                val oratorId = uri.lastPathSegment?.toLongOrNull()
                val title = uri.getQueryParameter("title")
                if (oratorId != null && !title.isNullOrBlank()) {
                    navController.navigateToFullSpeech(oratorId, title)
                    intent.data = null
                }
            }
        }
    }
}

private data class PrefsLoad(
    val ready: Boolean,
    val preferences: UserPreferencesEntity?,
)
