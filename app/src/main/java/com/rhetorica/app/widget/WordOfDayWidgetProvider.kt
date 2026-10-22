package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import com.rhetorica.app.R
import com.rhetorica.app.core.util.AppLog
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

/**
 * Home-screen widget. One card is laid out at a time: today's word, or today's
 * idiom when that card is still enabled. Arrow buttons change a stored index
 * and the widget is drawn again, wrapping from the last card back to the first.
 *
 * The other card is not in the view tree. A stack would keep it underneath, so
 * a translucent or fully transparent fill showed both texts at once. Swipe went
 * with the stack. These arrows are the way to change cards.
 *
 * The receiver class name is part of the placed-widget identity. Do not rename it.
 */
@AndroidEntryPoint
class WordOfDayWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var contentLoader: WidgetContentLoader

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_SHOW_NEXT -> flip(context, intent, next = true, pendingResult = goAsync())
            ACTION_SHOW_PREVIOUS -> flip(context, intent, next = false, pendingResult = goAsync())
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshScheduler.ensureScheduled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetRefreshScheduler.cancel(context)
        WidgetDeckIndexStore(context).clearAll()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val store = WidgetDeckIndexStore(context)
        appWidgetIds.forEach(store::clear)
    }

    /**
     * Fired when the user resizes the widget. The stored card stays put and
     * the text is laid out again for the new cell size.
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val appContext = context.applicationContext
        val store = WidgetDeckIndexStore(appContext)
        ioExecutor.execute {
            appWidgetIds.forEach { appWidgetId ->
                try {
                    val snapshot = loadSnapshot(appContext)
                    val index = storedIndex(store, appWidgetId, snapshot.cards.size)
                    val remoteViews = render(appContext, appWidgetId, snapshot, index)
                    mainHandler.post { applyUpdate(appWidgetManager, appWidgetId, remoteViews) }
                } catch (e: Exception) {
                    AppLog.e(TAG, "Failed to build widget $appWidgetId", e)
                    mainHandler.post {
                        applyUpdate(appWidgetManager, appWidgetId, WidgetCardRenderer.error(appContext))
                    }
                }
            }
        }
    }

    private fun flip(
        context: Context,
        intent: Intent,
        next: Boolean,
        pendingResult: BroadcastReceiver.PendingResult,
    ) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            pendingResult.finish()
            return
        }
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val store = WidgetDeckIndexStore(appContext)
        ioExecutor.execute {
            try {
                val snapshot = loadSnapshot(appContext)
                val index = WidgetDeckIndex.advance(store.get(appWidgetId), snapshot.cards.size, next)
                store.set(appWidgetId, index)
                val remoteViews = render(appContext, appWidgetId, snapshot, index)
                mainHandler.post {
                    applyUpdate(appWidgetManager, appWidgetId, remoteViews)
                    pendingResult.finish()
                }
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to flip widget $appWidgetId", e)
                mainHandler.post {
                    applyUpdate(appWidgetManager, appWidgetId, WidgetCardRenderer.error(appContext))
                    pendingResult.finish()
                }
            }
        }
    }

    private fun render(
        context: Context,
        appWidgetId: Int,
        snapshot: WidgetSnapshot,
        index: Int,
    ): RemoteViews {
        val card = WidgetDeck.visibleCard(snapshot.cards, index)
            ?: return WidgetCardRenderer.error(context)
        val showsFlipControls = snapshot.showsFlipControls
        val layout = layoutFor(context, appWidgetId)
        val background = WidgetCardRenderer.backgroundBitmap(context, layout, snapshot.chrome)
        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            WidgetCardRenderer.bind(
                views = this,
                context = context,
                appWidgetId = appWidgetId,
                card = card,
                layout = layout,
                background = background,
                showsFlipControls = showsFlipControls,
            )
            if (showsFlipControls) {
                setViewVisibility(R.id.widgetFlipBar, View.VISIBLE)
                setOnClickPendingIntent(R.id.widgetPrev, WidgetClicks.flip(context, appWidgetId, next = false))
                setOnClickPendingIntent(R.id.widgetNext, WidgetClicks.flip(context, appWidgetId, next = true))
            } else {
                setViewVisibility(R.id.widgetFlipBar, View.GONE)
            }
        }
    }

    private fun storedIndex(store: WidgetDeckIndexStore, appWidgetId: Int, count: Int): Int {
        val stored = store.get(appWidgetId)
        val clamped = WidgetDeckIndex.clamp(stored, count)
        if (clamped != stored) store.set(appWidgetId, clamped)
        return clamped
    }

    private fun loadSnapshot(context: Context): WidgetSnapshot {
        if (!::contentLoader.isInitialized) return WidgetContentLoader.unavailable(context)
        return runBlocking { contentLoader.load(context) }
    }

    private fun layoutFor(context: Context, appWidgetId: Int): WidgetLayoutSize {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        return WidgetLayoutPolicy.fromHostBounds(
            minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
            minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
            maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0),
            maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
        )
    }

    private fun applyUpdate(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        views: RemoteViews,
    ) {
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to apply widget update $appWidgetId", e)
        }
    }

    companion object {
        private const val TAG = "WordOfDayWidgetProvider"

        const val ACTION_OPEN_SPEECH_FROM_WIDGET = "com.rhetorica.app.widget.OPEN_SPEECH"
        const val EXTRA_ORATOR_ID = "oratorId"
        const val EXTRA_SPEECH_TITLE = "speechTitle"
        const val ACTION_SHOW_NEXT = "com.rhetorica.app.widget.SHOW_NEXT"
        const val ACTION_SHOW_PREVIOUS = "com.rhetorica.app.widget.SHOW_PREVIOUS"

        private val ioExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}
