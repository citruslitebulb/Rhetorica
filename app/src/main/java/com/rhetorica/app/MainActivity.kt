package com.rhetorica.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.rhetorica.app.notification.WordNotificationHelper
import com.rhetorica.app.ui.RhetoricaApp
import com.rhetorica.app.ui.theme.RhetoricaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (!isGranted) {
            android.util.Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Handle notification actions
        when (intent.action) {
            WordNotificationHelper.ACTION_MORE_INFO -> {
                val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
                if (wordId != -1L) {
                    // TODO: Navigate to word detail screen when implemented
                    android.util.Log.d("MainActivity", "Navigate to word detail: $wordId")
                }
            }
        }
        
        enableEdgeToEdge()
        setContent {
            RhetoricaTheme {
                RhetoricaApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (intent.action) {
            WordNotificationHelper.ACTION_MORE_INFO -> {
                val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
                if (wordId != -1L) {
                    // TODO: Navigate to word detail screen when implemented
                    android.util.Log.d("MainActivity", "Navigate to word detail: $wordId")
                }
            }
        }
    }
}
