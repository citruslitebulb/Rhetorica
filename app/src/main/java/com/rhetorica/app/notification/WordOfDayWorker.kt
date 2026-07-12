package com.rhetorica.app.notification

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.repository.WordRepository
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
            val preferences = userPreferencesDao.getUserPreferences()
            val word = wordRepository.getWordOfTheDayForPreferences(
                selectedOratorId = preferences?.selectedOratorId,
                rotateThroughAll = preferences?.rotateThroughAll ?: false,
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
                val notificationId = (word.id and Int.MAX_VALUE.toLong()).toInt()
                notificationManager.notify(notificationId, notification)

                WidgetAppearance.refreshAllWidgets(context)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            android.util.Log.e("WordOfDayWorker", "Failed to show word notification", e)
            Result.failure()
        }
    }
}
