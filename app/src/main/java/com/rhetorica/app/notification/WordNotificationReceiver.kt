package com.rhetorica.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.rhetorica.app.R
import com.rhetorica.app.core.tts.TtsSpeaker
import com.rhetorica.app.data.local.SavedWordDao
import com.rhetorica.app.data.local.SavedWordEntity
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.repository.ProgressRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WordNotificationReceiver : BroadcastReceiver() {
    @Inject
    lateinit var savedWordDao: SavedWordDao

    @Inject
    lateinit var wordDao: WordDao

    @Inject
    lateinit var ttsSpeaker: TtsSpeaker

    @Inject
    lateinit var progressRepository: ProgressRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        // Scope lifetime is tied to this goAsync() call via finally { finish() }.
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        when (intent.action) {
            WordNotificationHelper.ACTION_FAVORITE -> {
                val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
                if (wordId == -1L) {
                    pendingResult.finish()
                    return
                }
                scope.launch {
                    try {
                        savedWordDao.saveWord(
                            SavedWordEntity(
                                wordId = wordId,
                                savedAtEpochMillis = System.currentTimeMillis(),
                            ),
                        )
                        progressRepository.syncSavedCount()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.notification_word_saved),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Failed to save word", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.notification_word_save_failed),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            WordNotificationHelper.ACTION_HEAR -> {
                val wordText = intent.getStringExtra(WordNotificationHelper.WORD_TEXT_EXTRA)
                val wordId = intent.getLongExtra(WordNotificationHelper.WORD_ID_EXTRA, -1L)
                if (wordText.isNullOrBlank()) {
                    pendingResult.finish()
                    return
                }
                scope.launch {
                    try {
                        val definition = if (wordId != -1L) {
                            wordDao.getWordById(wordId)?.definition.orEmpty()
                        } else {
                            ""
                        }
                        // Keep goAsync alive until TTS finishes (or times out).
                        ttsSpeaker.speakWordAndDefinitionAndAwait(wordText, definition)
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Failed to speak word", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            else -> pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "WordNotificationReceiver"
    }
}
