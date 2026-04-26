package com.rhetorica.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object NotificationChannelManager {
    private const val WORD_OF_DAY_CHANNEL_ID = "word_of_day_channel"
    private const val WORD_OF_DAY_CHANNEL_NAME = "Word of the Day"
    private const val WORD_OF_DAY_CHANNEL_DESCRIPTION = "Daily vocabulary notifications"

    fun createWordOfDayChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WORD_OF_DAY_CHANNEL_ID,
                WORD_OF_DAY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = WORD_OF_DAY_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService<NotificationManager>()
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun getWordOfDayChannelId(): String = WORD_OF_DAY_CHANNEL_ID
}
