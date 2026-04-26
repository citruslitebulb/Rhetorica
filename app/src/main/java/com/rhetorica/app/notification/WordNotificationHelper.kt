package com.rhetorica.app.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.rhetorica.app.R
import com.rhetorica.app.MainActivity

object WordNotificationHelper {
    const val ACTION_FAVORITE = "com.rhetorica.app.ACTION_FAVORITE"
    const val ACTION_MORE_INFO = "com.rhetorica.app.ACTION_MORE_INFO"
    const val ACTION_HEAR = "com.rhetorica.app.ACTION_HEAR"
    const val WORD_ID_EXTRA = "word_id"
    const val WORD_TEXT_EXTRA = "word_text"

    fun createWordNotification(
        context: Context,
        word: String,
        definition: String,
        example: String,
        wordId: Long,
    ): Notification {
        val channelId = NotificationChannelManager.getWordOfDayChannelId()

        // Main intent to open the app
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // Favorite action
        val favoriteIntent = Intent(context, WordNotificationReceiver::class.java).apply {
            action = ACTION_FAVORITE
            putExtra(WORD_ID_EXTRA, wordId)
        }
        val favoritePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            favoriteIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // More info action
        val moreInfoIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_MORE_INFO
            putExtra(WORD_ID_EXTRA, wordId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val moreInfoPendingIntent = PendingIntent.getActivity(
            context,
            2,
            moreInfoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        // Hear action (TTS placeholder)
        val hearIntent = Intent(context, WordNotificationReceiver::class.java).apply {
            action = ACTION_HEAR
            putExtra(WORD_ID_EXTRA, wordId)
            putExtra(WORD_TEXT_EXTRA, word)
        }
        val hearPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            hearIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(word)
            .setContentText(definition)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$definition\n\nExample: $example")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.star_on,
                "Favorite",
                favoritePendingIntent,
            )
            .addAction(
                android.R.drawable.ic_menu_info_details,
                "More info",
                moreInfoPendingIntent,
            )
            .addAction(
                android.R.drawable.ic_lock_silent_mode_off,
                "Hear",
                hearPendingIntent,
            )
            .build()
    }
}
