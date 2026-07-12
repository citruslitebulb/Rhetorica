package com.rhetorica.app.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.rhetorica.app.MainActivity
import com.rhetorica.app.R

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

        // Body tap → word detail (same path as "More info"). Prefer CLEAR_TOP over CLEAR_TASK
        // so we do not wipe the entire back stack when the app is already open.
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_MORE_INFO
            data = Uri.parse("rhetorica://word/$wordId")
            putExtra(WORD_ID_EXTRA, wordId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            (wordId % Int.MAX_VALUE).toInt(),
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val favoriteIntent = Intent(context, WordNotificationReceiver::class.java).apply {
            action = ACTION_FAVORITE
            putExtra(WORD_ID_EXTRA, wordId)
        }
        val favoritePendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(wordId, 1),
            favoriteIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val moreInfoIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_MORE_INFO
            data = Uri.parse("rhetorica://word/$wordId")
            putExtra(WORD_ID_EXTRA, wordId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val moreInfoPendingIntent = PendingIntent.getActivity(
            context,
            requestCode(wordId, 2),
            moreInfoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val hearIntent = Intent(context, WordNotificationReceiver::class.java).apply {
            action = ACTION_HEAR
            putExtra(WORD_ID_EXTRA, wordId)
            putExtra(WORD_TEXT_EXTRA, word)
        }
        val hearPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(wordId, 3),
            hearIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(word)
            .setContentText(definition)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$definition\n\nExample: $example"),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.star_on,
                context.getString(R.string.notification_action_favorite),
                favoritePendingIntent,
            )
            .addAction(
                android.R.drawable.ic_menu_info_details,
                context.getString(R.string.notification_action_more_info),
                moreInfoPendingIntent,
            )
            .addAction(
                android.R.drawable.ic_lock_silent_mode_off,
                context.getString(R.string.notification_action_hear),
                hearPendingIntent,
            )
            .build()
    }

    private fun requestCode(wordId: Long, actionOrdinal: Int): Int {
        return (31 * (wordId % Int.MAX_VALUE).toInt()) + actionOrdinal
    }
}
