package com.rhetorica.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WordNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        when (intent.action) {
            WordNotificationHelper.ACTION_FAVORITE -> {
                val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
                if (wordId != -1L) {
                    // TODO: Implement save word logic
                    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    scope.launch {
                        try {
                            // repository.saveWord(wordId)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                    Toast.makeText(context, "Word saved to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    pendingResult.finish()
                }
            }
            WordNotificationHelper.ACTION_HEAR -> {
                val wordText = intent.getStringExtra(WordNotificationHelper.WORD_TEXT_EXTRA)
                if (wordText != null) {
                    // TODO: Implement TextToSpeech
                    Toast.makeText(context, "Speaking: $wordText", Toast.LENGTH_SHORT).show()
                }
                pendingResult.finish()
            }
            null -> {
                pendingResult.finish()
            }
            else -> {
                pendingResult.finish()
            }
        }
    }
}
