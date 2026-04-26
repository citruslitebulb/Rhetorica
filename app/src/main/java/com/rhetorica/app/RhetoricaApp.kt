package com.rhetorica.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rhetorica.app.notification.NotificationChannelManager
import com.rhetorica.app.notification.WordOfDayWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class RhetoricaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createWordOfDayChannel(this)
        scheduleWordOfDayNotification()
    }

    private fun scheduleWordOfDayNotification() {
        val workRequest = PeriodicWorkRequestBuilder<WordOfDayWorker>(
            24, TimeUnit.HOURS,
        )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "word_of_day_notification",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest,
        )
    }
}
