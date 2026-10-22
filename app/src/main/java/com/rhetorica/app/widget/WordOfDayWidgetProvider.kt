package com.rhetorica.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import com.rhetorica.app.MainActivity
import com.rhetorica.app.R
import com.rhetorica.app.core.util.AppLog
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

/**
 * Home-screen widget. A stack flips between today's word and today's idiom.
 * Arrow buttons advance that stack for launchers where the swipe is awkward.
 *
 * The receiver class name is part of the placed-widget identity. Do not rename it.
 *
 * The collection methods used here are the RemoteViewsService entry points.
 * Their API 31 replacements build the items inline and drop the service.
 */
@Suppress("DEPRECATION")
@AndroidEntryPoint
class WordOfDayWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var contentLoader: WidgetContentLoader

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_SHOW_NEXT -> flip(context, intent, next = true)
            ACTION_SHOW_PREVIOUS -> flip(context, intent, next = false)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds, showLoadingFirst = true)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshScheduler.ensureScheduled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefreshScheduler.cancel(context)
    }

    /**
     * Fired when the user resizes the widget. Reload the cards in place so the
     * visible card stays put and both cards pick up the new cell size.
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetStack)
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        showLoadingFirst: Boolean,
    ) {
        if (showLoadingFirst) {
            appWidgetIds.forEach { appWidgetId ->
                try {
                    appWidgetManager.updateAppWidget(appWidgetId, WidgetCardRenderer.loading(context))
                } catch (e: Exception) {
                    AppLog.e(TAG, "Failed to push loading layout for $appWidgetId", e)
                }
            }
        }

        val appContext = context.applicationContext
        ioExecutor.execute {
            appWidgetIds.forEach { appWidgetId ->
                try {
                    val remoteViews = createRemoteViews(appContext, appWidgetId)
                    mainHandler.post {
                        try {
                            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetStack)
                        } catch (e: Exception) {
                            AppLog.e(TAG, "Failed to apply widget update $appWidgetId", e)
                        }
                    }
                } catch (e: Exception) {
                    AppLog.e(TAG, "Failed to build widget $appWidgetId", e)
                    mainHandler.post {
                        try {
                            appWidgetManager.updateAppWidget(appWidgetId, WidgetCardRenderer.error(appContext))
                        } catch (inner: Exception) {
                            AppLog.e(TAG, "Failed to apply error layout $appWidgetId", inner)
                        }
                    }
                }
            }
        }
    }

    private fun createRemoteViews(context: Context, appWidgetId: Int): RemoteViews {
        val showsFlipControls = loadShowsFlipControls(context)
        return RemoteViews(context.packageName, R.layout.widget_deck).apply {
            setRemoteAdapter(R.id.widgetStack, adapterIntent(context, appWidgetId))
            setPendingIntentTemplate(R.id.widgetStack, templatePendingIntent(context, appWidgetId))
            if (showsFlipControls) {
                setViewVisibility(R.id.widgetFlipBar, View.VISIBLE)
                setOnClickPendingIntent(
                    R.id.widgetPrev,
                    flipPendingIntent(context, appWidgetId, ACTION_SHOW_PREVIOUS),
                )
                setOnClickPendingIntent(
                    R.id.widgetNext,
                    flipPendingIntent(context, appWidgetId, ACTION_SHOW_NEXT),
                )
            } else {
                setViewVisibility(R.id.widgetFlipBar, View.GONE)
            }
        }
    }

    private fun loadShowsFlipControls(context: Context): Boolean {
        if (!::contentLoader.isInitialized) return false
        val snapshot = runBlocking { contentLoader.load(context) }
        return snapshot.showsFlipControls
    }

    private fun flip(context: Context, intent: Intent, next: Boolean) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val views = RemoteViews(context.packageName, R.layout.widget_deck).apply {
            if (next) showNext(R.id.widgetStack) else showPrevious(R.id.widgetStack)
        }
        try {
            AppWidgetManager.getInstance(context).partiallyUpdateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to flip widget $appWidgetId", e)
        }
    }

    private fun adapterIntent(context: Context, appWidgetId: Int): Intent {
        return Intent(context, WordOfDayWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse("rhetorica://widget/deck/$appWidgetId")
        }
    }

    private fun templatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = OPEN_APP_FLAGS
        }
        return PendingIntent.getActivity(
            context,
            TEMPLATE_REQUEST_BASE + appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    private fun flipPendingIntent(context: Context, appWidgetId: Int, action: String): PendingIntent {
        val intent = Intent(context, WordOfDayWidgetProvider::class.java).apply {
            this.action = action
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse("rhetorica://widget/$appWidgetId/$action")
        }
        val requestCode = appWidgetId * 2 + if (action == ACTION_SHOW_NEXT) 1 else 0
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val TAG = "WordOfDayWidgetProvider"

        const val ACTION_OPEN_SPEECH_FROM_WIDGET = "com.rhetorica.app.widget.OPEN_SPEECH"
        const val EXTRA_ORATOR_ID = "oratorId"
        const val EXTRA_SPEECH_TITLE = "speechTitle"
        const val ACTION_SHOW_NEXT = "com.rhetorica.app.widget.SHOW_NEXT"
        const val ACTION_SHOW_PREVIOUS = "com.rhetorica.app.widget.SHOW_PREVIOUS"

        private const val TEMPLATE_REQUEST_BASE = 50_000

        private const val OPEN_APP_FLAGS =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

        private val ioExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}
