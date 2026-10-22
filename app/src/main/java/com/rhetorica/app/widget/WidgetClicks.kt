package com.rhetorica.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.rhetorica.app.MainActivity

/** Explicit click intents for the single widget card and its arrow buttons. */
internal object WidgetClicks {
    fun open(context: Context, requestCode: Int, uri: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = OPEN_APP_FLAGS
            if (uri != null) data = Uri.parse(uri)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun flip(context: Context, appWidgetId: Int, next: Boolean): PendingIntent {
        val action = if (next) {
            WordOfDayWidgetProvider.ACTION_SHOW_NEXT
        } else {
            WordOfDayWidgetProvider.ACTION_SHOW_PREVIOUS
        }
        val intent = Intent(context, WordOfDayWidgetProvider::class.java).apply {
            this.action = action
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse("rhetorica://widget/$appWidgetId/$action")
        }
        val requestCode = if (next) {
            WidgetPendingIntentIds.next(appWidgetId)
        } else {
            WidgetPendingIntentIds.previous(appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val OPEN_APP_FLAGS =
        Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP
}
