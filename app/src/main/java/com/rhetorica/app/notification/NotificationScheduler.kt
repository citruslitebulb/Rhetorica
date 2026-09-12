package com.rhetorica.app.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rhetorica.app.core.util.AppLog
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Daily Word of the Day notification at a user-chosen local time.
 *
 * Each run is a one-shot request delayed to the next occurrence of the chosen
 * clock time; [WordOfDayWorker] enqueues the following day when it finishes.
 * (A 24h `PeriodicWorkRequest` drifts later every day because each run is
 * scheduled relative to when the previous one actually executed.)
 *
 * App launch uses [ExistingWorkPolicy.KEEP] so opening the app does not push
 * the pending fire. Preference changes call [reschedule].
 */
object NotificationScheduler {
    const val UNIQUE_WORK_NAME = "word_of_day_notification"
    const val NOTIFICATION_ID = 1001

    private const val PREFS_NAME = "rhetorica_notifications"
    private const val KEY_SIGNATURE = "schedule_signature"

    fun millisUntilNext(
        hour: Int,
        minute: Int,
        now: ZonedDateTime = ZonedDateTime.now(),
    ): Long {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        var next = now.withHour(safeHour).withMinute(safeMinute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return Duration.between(now, next).toMillis().coerceAtLeast(1L)
    }

    /**
     * Identifies the current schedule. The `v2` prefix distinguishes the one-shot
     * chain from the old periodic request so upgrades replace it instead of keeping it.
     */
    fun scheduleSignature(enabled: Boolean, hour: Int, minute: Int): String {
        return "v2:$enabled:${hour.coerceIn(0, 23)}:${minute.coerceIn(0, 59)}"
    }

    fun ensureScheduled(
        context: Context,
        enabled: Boolean,
        hour: Int,
        minute: Int,
    ) {
        applySchedule(context, enabled, hour, minute, force = false)
    }

    fun reschedule(
        context: Context,
        enabled: Boolean,
        hour: Int,
        minute: Int,
    ) {
        applySchedule(context, enabled, hour, minute, force = true)
    }

    /**
     * Called by [WordOfDayWorker] after a run to line up tomorrow. Appends behind the
     * finishing request instead of replacing it, so the running worker is not cancelled.
     */
    fun scheduleNextFromWorker(context: Context, hour: Int, minute: Int) {
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            buildRequest(hour, minute),
        )
        AppLog.i(TAG, "Queued next daily notification (${scheduleSignature(true, hour, minute)})")
    }

    private fun applySchedule(
        context: Context,
        enabled: Boolean,
        hour: Int,
        minute: Int,
        force: Boolean,
    ) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val signature = scheduleSignature(enabled, hour, minute)
        val stored = prefs.getString(KEY_SIGNATURE, null)

        if (!enabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            prefs.edit().putString(KEY_SIGNATURE, signature).apply()
            AppLog.i(TAG, "Notifications disabled; cancelled unique work")
            return
        }

        if (!force && stored == signature) {
            // KEEP only retains a request that is still pending; if the chain was lost
            // (e.g. app data restore), this quietly re-arms it.
            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                buildRequest(hour, minute),
            )
            AppLog.i(TAG, "Keeping existing notification schedule ($signature)")
            return
        }

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            buildRequest(hour, minute),
        )
        prefs.edit().putString(KEY_SIGNATURE, signature).apply()
        AppLog.i(TAG, "Scheduled daily notification ($signature)")
    }

    private fun buildRequest(hour: Int, minute: Int) =
        OneTimeWorkRequestBuilder<WordOfDayWorker>()
            .setInitialDelay(millisUntilNext(hour, minute), TimeUnit.MILLISECONDS)
            .build()

    private const val TAG = "NotificationScheduler"
}
