package com.rhetorica.app.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rhetorica.app.core.util.AppLog
import com.rhetorica.app.data.repository.WordRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Picks today's word (if not already picked) and repaints every placed widget. */
@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val wordRepository: WordRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            wordRepository.ensureTodaysWord()
            WidgetAppearance.refreshAllWidgets(context)
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to refresh widgets at midnight", e)
        }
        WidgetRefreshScheduler.scheduleNextFromWorker(context)
        return Result.success()
    }

    private companion object {
        const val TAG = "WidgetRefreshWorker"
    }
}
