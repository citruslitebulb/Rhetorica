package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rhetorica.app.core.util.AppLog
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Rolls the home-screen widget over to the new Word of the Day shortly after local
 * midnight, independent of whether the daily notification is enabled.
 *
 * Without this the widget only refreshed on app launch, on a preference change, or
 * when the notification worker ran, so users with notifications off could see
 * yesterday's word well into the day.
 */
object WidgetRefreshScheduler {
    const val UNIQUE_WORK_NAME = "word_of_day_widget_refresh"

    /** Small buffer so the run lands safely inside the new calendar day. */
    private val midnightGrace: Duration = Duration.ofMinutes(2)

    fun millisUntilNextMidnight(now: ZonedDateTime = ZonedDateTime.now()): Long {
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).plus(midnightGrace)
        return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1L)
    }

    fun hasWidgets(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        val componentName = ComponentName(context, WordOfDayWidgetProvider::class.java)
        return manager.getAppWidgetIds(componentName).isNotEmpty()
    }

    /** Arms the next midnight refresh if any widget is placed; no-op otherwise. */
    fun ensureScheduled(context: Context) {
        if (!hasWidgets(context)) return
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            buildRequest(),
        )
        AppLog.i(TAG, "Widget midnight refresh armed")
    }

    /** Called by [WidgetRefreshWorker] to chain the following night. */
    fun scheduleNextFromWorker(context: Context) {
        if (!hasWidgets(context)) return
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            buildRequest(),
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
        AppLog.i(TAG, "Widget midnight refresh cancelled")
    }

    private fun buildRequest() =
        OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setInitialDelay(millisUntilNextMidnight(), TimeUnit.MILLISECONDS)
            .build()

    private const val TAG = "WidgetRefreshScheduler"
}
