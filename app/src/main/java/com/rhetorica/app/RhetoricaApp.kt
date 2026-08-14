package com.rhetorica.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.rhetorica.app.core.tts.TtsSpeaker
import com.rhetorica.app.core.util.AppLog
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.orDefault
import com.rhetorica.app.data.repository.WordRepository
import com.rhetorica.app.data.seed.SeedDataLoader
import com.rhetorica.app.notification.NotificationChannelManager
import com.rhetorica.app.notification.NotificationScheduler
import com.rhetorica.app.widget.WidgetAppearance
import dagger.hilt.android.HiltAndroidApp
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

    @Inject
    lateinit var userPreferencesDao: UserPreferencesDao

    @Inject
    lateinit var wordRepository: WordRepository

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
                wordRepository.ensureTodaysWord()
                WidgetAppearance.refreshAllWidgets(this@RhetoricaApp)
            } catch (e: Exception) {
                AppLog.e("RhetoricaApp", "Failed to load seed data", e)
            }
        }
    }

    private fun scheduleWordOfDayNotification() {
        applicationScope.launch {
            val preferences = try {
                userPreferencesDao.getUserPreferences().orDefault()
            } catch (e: Exception) {
                AppLog.e("RhetoricaApp", "Failed to read notification preferences", e)
                return@launch
            }
            NotificationScheduler.ensureScheduled(
                context = this@RhetoricaApp,
                enabled = preferences.onboardingCompleted && preferences.notificationsEnabled,
                hour = preferences.notificationHour,
                minute = preferences.notificationMinute,
            )
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsSpeaker.shutdown()
        applicationScope.cancel()
    }
}
