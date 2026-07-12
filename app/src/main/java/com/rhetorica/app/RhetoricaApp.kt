package com.rhetorica.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rhetorica.app.core.tts.TtsSpeaker
import com.rhetorica.app.data.seed.SeedDataLoader
import com.rhetorica.app.notification.NotificationChannelManager
import com.rhetorica.app.notification.WordOfDayWorker
import com.rhetorica.app.widget.WidgetAppearance
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@HiltAndroidApp
class RhetoricaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var seedDataLoader: SeedDataLoader

    @Inject
    lateinit var ttsSpeaker: TtsSpeaker

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createWordOfDayChannel(this)
        scheduleWordOfDayNotification()
        loadSeedData()
    }

    private fun loadSeedData() {
        applicationScope.launch {
            try {
                seedDataLoader.loadSeedDataIfNeeded()
                // Ensure any placed Word of the Day widgets get populated/updated promptly
                // after (re)seeding completes. Critical on first run when widget may be added
                // before or during the async seed, and on day rollover or new seed data.
                WidgetAppearance.refreshAllWidgets(this@RhetoricaApp)
            } catch (e: Exception) {
                android.util.Log.e("RhetoricaApp", "Failed to load seed data", e)
            }
        }
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

    override fun onTerminate() {
        super.onTerminate()
        ttsSpeaker.shutdown()
        applicationScope.cancel()
    }
}
