package com.rhetorica.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.rhetorica.app.data.local.SavedWordEntity
import com.rhetorica.app.data.local.SavedWordDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class WordNotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var savedWordDao: SavedWordDao

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        when (intent.action) {
            WordNotificationHelper.ACTION_FAVORITE -> {
                val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
                if (wordId != -1L) {
                    try {
                        runBlocking(Dispatchers.IO) {
                            savedWordDao.saveWord(
                                SavedWordEntity(
                                    wordId = wordId,
                                    savedAtEpochMillis = System.currentTimeMillis()
                                )
                            )
                        }
                        Toast.makeText(context, "Word saved to favorites", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("WordNotificationReceiver", "Failed to save word", e)
                        Toast.makeText(context, "Failed to save word", Toast.LENGTH_SHORT).show()
                    } finally {
                        pendingResult.finish()
                    }
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
