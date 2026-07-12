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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rhetorica.app.feature.speech.navigateToFullSpeech
import com.rhetorica.app.feature.word.navigateToWordDetail
import com.rhetorica.app.notification.WordNotificationHelper
import com.rhetorica.app.ui.RhetoricaApp
import com.rhetorica.app.ui.theme.RhetoricaTheme
import com.rhetorica.app.widget.WordOfDayWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val incomingIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* granted or denied — notifications simply won't show if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            RhetoricaTheme {
                val navController = rememberNavController()
                // Capture launch intent once so recomposition cannot re-read a cleared intent.
                val launchIntent = remember { intent }

                LaunchedEffect(navController) {
                    // Wait until NavHost has registered the start destination so navigate() is safe.
                    navController.currentBackStackEntryFlow.first()

                    handleNavigationIntent(launchIntent, navController)

                    incomingIntents.collect { navIntent ->
                        handleNavigationIntent(navIntent, navController)
                    }
                }

                RhetoricaApp(navController = navController)
            }
        }

        // Request notification permission after UI is ready to avoid startup ANR pressure.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Handler(Looper.getMainLooper()).postDelayed({
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }, 400)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingIntents.tryEmit(intent)
    }

    private fun handleNavigationIntent(intent: Intent?, navController: NavHostController) {
        if (intent == null) return

        // Widget "Tap for full speech" CTA
        if (intent.action == WordOfDayWidgetProvider.ACTION_OPEN_SPEECH_FROM_WIDGET) {
            val oratorId = intent.getLongExtra(WordOfDayWidgetProvider.EXTRA_ORATOR_ID, -1L)
            val speechTitle = intent.getStringExtra(WordOfDayWidgetProvider.EXTRA_SPEECH_TITLE)
            if (oratorId != -1L && !speechTitle.isNullOrBlank()) {
                navController.navigateToFullSpeech(oratorId, speechTitle)
                intent.action = null
                return
            }
        }

        // Notification body / "More info" → word detail
        if (intent.action == WordNotificationHelper.ACTION_MORE_INFO) {
            val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
            if (wordId != -1L) {
                navController.navigateToWordDetail(wordId)
                intent.action = null
                return
            }
        }

        // Internal deep link: rhetorica://word/{id} (explicit PendingIntent only; no VIEW filter)
        intent.data?.let { uri ->
            if (uri.scheme == "rhetorica" && uri.host == "word") {
                uri.lastPathSegment?.toLongOrNull()?.let { wordId ->
                    navController.navigateToWordDetail(wordId)
                    intent.data = null
                    return
                }
            }
            // Internal deep link: rhetorica://speech/{oratorId}?title=...
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
