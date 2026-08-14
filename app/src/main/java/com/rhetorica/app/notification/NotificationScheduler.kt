package com.rhetorica.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rhetorica.app.core.util.AppLog
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Daily Word of the Day notification at a user-chosen local time.
 *
 * App launch uses [ExistingPeriodicWorkPolicy.KEEP] so opening the app does not
 * push the next fire. Preference changes call [reschedule].
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

    fun scheduleSignature(enabled: Boolean, hour: Int, minute: Int): String {
        return "$enabled:${hour.coerceIn(0, 23)}:${minute.coerceIn(0, 59)}"
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
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                buildRequest(hour, minute),
            )
            AppLog.i(TAG, "Keeping existing notification schedule ($signature)")
            return
        }

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildRequest(hour, minute),
        )
        prefs.edit().putString(KEY_SIGNATURE, signature).apply()
        AppLog.i(TAG, "Scheduled daily notification ($signature)")
    }

    private fun buildRequest(hour: Int, minute: Int) =
        PeriodicWorkRequestBuilder<WordOfDayWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNext(hour, minute), TimeUnit.MILLISECONDS)
            .build()

    private const val TAG = "NotificationScheduler"
}
