package com.rhetorica.app.notification

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rhetorica.app.data.local.WordDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WordOfDayWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val wordDao: WordDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val count = wordDao.wordCount()

            if (count == 0) {
                Result.failure()
            } else {
                val dayOfYear = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).dayOfYear
                val offset = (dayOfYear - 1) % count
                val word = wordDao.getWordOfTheDay(offset)

                if (word != null) {
                    val notification = WordNotificationHelper.createWordNotification(
                        context = context,
                        word = word.word,
                        definition = word.definition,
                        example = word.example,
                        wordId = word.id,
                    )

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationId = (word.id and Int.MAX_VALUE.toLong()).toInt()
                    notificationManager.notify(notificationId, notification)

                    Result.success()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WordOfDayWorker", "Failed to show word notification", e)
            Result.failure()
        }
    }
}
