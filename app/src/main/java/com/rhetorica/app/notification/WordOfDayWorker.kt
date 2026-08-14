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
        return try {
            val preferences = userPreferencesDao.getUserPreferences().orDefault()
            if (!preferences.notificationsEnabled) {
                return Result.success()
            }

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
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            AppLog.e("WordOfDayWorker", "Failed to show word notification", e)
            Result.failure()
        }
    }
}
