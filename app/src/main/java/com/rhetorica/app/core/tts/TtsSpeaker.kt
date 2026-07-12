package com.rhetorica.app.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Application-scoped TextToSpeech helper safe to call from Activities and Receivers.
 *
 * A single [engineMutex] serializes init, speak kickoff, and shutdown. Await paths
 * release the mutex after [TextToSpeech.speak] is enqueued so concurrent callers are
 * not blocked for the full utterance-start window.
 */
@Singleton
class TtsSpeaker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var tts: TextToSpeech? = null
    private val ready = AtomicBoolean(false)
    private val pendingText = AtomicReference<String?>(null)
    private val engineMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun speak(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        // Latest fire-and-forget wins if multiple speak() calls race before init finishes.
        pendingText.set(trimmed)
        scope.launch {
            engineMutex.withLock {
                if (!ensureReadyLocked()) {
                    pendingText.set(null)
                    return@withLock
                }
                val toSpeak = pendingText.getAndSet(null) ?: trimmed
                speakInternal(toSpeak, utteranceId = UTTERANCE_ID)
            }
        }
    }

    fun speakWordAndDefinition(word: String, definition: String) {
        speak(buildSpokenText(word, definition))
    }

    /**
     * Speaks [word] and [definition], suspending until the utterance **starts**, errors,
     * or [timeoutMs] elapses. The engine mutex is held only for init + enqueue; the
     * wait for [UtteranceProgressListener.onStart] runs unlocked so UI Speak is not blocked.
     */
    suspend fun speakWordAndDefinitionAndAwait(
        word: String,
        definition: String,
        timeoutMs: Long = DEFAULT_AWAIT_START_TIMEOUT_MS,
        awaitStartOnly: Boolean = true,
    ): Boolean {
        val text = buildSpokenText(word, definition)
        if (text.isBlank()) return false

        return withTimeoutOrNull(timeoutMs) {
            val result = CompletableDeferred<Boolean>()
            val utteranceId = "rhetorica_tts_${System.nanoTime()}"

            val enqueued = engineMutex.withLock {
                if (!ensureReadyLocked()) return@withLock false
                val engine = tts
                if (engine == null || !ready.get()) return@withLock false

                engine.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {
                            if (awaitStartOnly && id == utteranceId) {
                                result.complete(true)
                            }
                        }

                        override fun onDone(id: String?) {
                            if (!awaitStartOnly && id == utteranceId) {
                                result.complete(true)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(id: String?) {
                            if (id == utteranceId) {
                                result.complete(false)
                            }
                        }

                        override fun onError(id: String?, errorCode: Int) {
                            if (id == utteranceId) {
                                result.complete(false)
                            }
                        }
                    },
                )

                try {
                    val speakResult = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                    if (speakResult == TextToSpeech.ERROR) {
                        result.complete(false)
                        return@withLock false
                    }
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to speak", e)
                    result.complete(false)
                    false
                }
            }

            if (!enqueued) return@withTimeoutOrNull false
            // Mutex released — wait for start/done without blocking other speak calls.
            result.await()
        } ?: false
    }

    private fun buildSpokenText(word: String, definition: String): String {
        return buildString {
            append(word.trim())
            val def = definition.trim()
            if (def.isNotEmpty()) {
                append(". ")
                append(def)
            }
        }.trim()
    }

    /**
     * Ensures a single shared [TextToSpeech] instance is initialized.
     * Caller must already hold [engineMutex].
     */
    private suspend fun ensureReadyLocked(): Boolean {
        if (ready.get() && tts != null) return true

        shutdownEngineLocked()

        val success = suspendCancellableCoroutine { cont ->
            val engine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ready.set(true)
                    if (cont.isActive) cont.resume(true)
                } else {
                    ready.set(false)
                    Log.e(TAG, "TextToSpeech init failed with status=$status")
                    if (cont.isActive) cont.resume(false)
                }
            }
            tts = engine
            cont.invokeOnCancellation {
                if (!ready.get()) {
                    try {
                        engine.shutdown()
                    } catch (_: Exception) {
                        // ignore
                    }
                    if (tts === engine) {
                        tts = null
                    }
                }
            }
        }

        if (success) {
            tts?.language = Locale.US
        } else {
            shutdownEngineLocked()
        }
        return success
    }

    private fun speakInternal(text: String, utteranceId: String) {
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to speak", e)
        }
    }

    private fun shutdownEngineLocked() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
            // ignore
        } finally {
            tts = null
            ready.set(false)
        }
    }

    fun shutdown() {
        scope.launch {
            engineMutex.withLock {
                shutdownEngineLocked()
                pendingText.set(null)
            }
        }
    }

    companion object {
        private const val TAG = "TtsSpeaker"
        private const val UTTERANCE_ID = "rhetorica_tts"
        /** Wait for utterance to start (not finish) — enough for goAsync safety. */
        private const val DEFAULT_AWAIT_START_TIMEOUT_MS = 8_000L
    }
}
