package com.rhetorica.app.notification

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rhetorica.app.core.util.AppLog
import com.rhetorica.app.data.local.orDefault
import com.rhetorica.app.data.repository.WordRepository
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.widget.WidgetAppearance
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WordOfDayWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val wordRepository: WordRepository,
    private val userPreferencesDao: UserPreferencesDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val preferences = try {
            userPreferencesDao.getUserPreferences().orDefault()
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to read preferences", e)
            return Result.failure()
        }
        if (!preferences.notificationsEnabled) {
            return Result.success()
        }

        try {
            val word = wordRepository.getWordOfTheDayForPreferences(
                selectedOratorId = preferences.selectedOratorId,
                rotateThroughAll = preferences.rotateThroughAll,
            )

            if (word != null) {
                val notification = WordNotificationHelper.createWordNotification(
                    context = context,
                    word = word.word,
                    definition = word.definition,
                    example = word.example,
                    wordId = word.id,
                )
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NotificationScheduler.NOTIFICATION_ID, notification)
                WidgetAppearance.refreshAllWidgets(context)
            } else {
                AppLog.w(TAG, "No Word of the Day available; skipping today's notification")
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to show word notification", e)
        }

        // Always line up tomorrow, even after a bad day, so one failure does not
        // silently end the daily habit.
        NotificationScheduler.scheduleNextFromWorker(
            context = context,
            hour = preferences.notificationHour,
            minute = preferences.notificationMinute,
        )
        return Result.success()
    }

    private companion object {
        const val TAG = "WordOfDayWorker"
    }
}
