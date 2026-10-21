package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.rhetorica.app.R
import com.rhetorica.app.core.util.AppLog
import kotlinx.coroutines.runBlocking

/**
 * One row per card. [onDataSetChanged] reloads today's word and idiom, including
 * after the midnight refresh and after a resize.
 */
class WordOfDayWidgetFactory(
    private val context: Context,
    private val appWidgetId: Int,
    private val loader: WidgetContentLoader?,
) : RemoteViewsService.RemoteViewsFactory {

    private var cards: List<WidgetDeckCard> = emptyList()
    private var showsFlipControls: Boolean = false
    private var background: Bitmap? = null

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        val snapshot = loadSnapshot()
        cards = snapshot.cards
        showsFlipControls = snapshot.showsFlipControls
        background = renderBackground(snapshot.chrome)
    }

    override fun onDestroy() {
        cards = emptyList()
        background = null
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        val card = cards.getOrNull(position) ?: return WidgetCardRenderer.error(context)
        return WidgetCardRenderer.bind(
            context = context,
            card = card,
            layout = currentLayout(),
            background = background,
            showsFlipControls = showsFlipControls,
        )
    }

    override fun getLoadingView(): RemoteViews = WidgetCardRenderer.loading(context)

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = cards.getOrNull(position)?.itemId ?: position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun loadSnapshot(): WidgetSnapshot {
        val contentLoader = loader ?: return WidgetContentLoader.unavailable(context)
        return try {
            runBlocking { contentLoader.load(context) }
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to load widget cards", e)
            WidgetContentLoader.unavailable(
                context = context,
                body = context.getString(R.string.widget_error_body),
            )
        }
    }

    private fun currentLayout(): WidgetLayoutSize {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        return WidgetLayoutPolicy.fromHostBounds(
            minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
            minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
            maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0),
            maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
        )
    }

    private fun renderBackground(chrome: WidgetChrome): Bitmap? {
        return try {
            val layout = currentLayout()
            val density = context.resources.displayMetrics.density
            val widthPx = (layout.widthDp * density).toInt().coerceAtLeast(1)
            val heightPx = (layout.heightDp * density).toInt().coerceAtLeast(1)
            val cornerRadiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                18f,
                context.resources.displayMetrics,
            )
            WidgetAppearance.createCardBitmap(
                context = context,
                widthPx = widthPx,
                heightPx = heightPx,
                cornerRadiusPx = cornerRadiusPx,
                fillColorArgb = WidgetAppearance.argbColorInt(
                    colorValue = chrome.backgroundColor,
                    opacityPercent = chrome.opacityPercent,
                ),
                imageKey = chrome.imageKey,
                galleryUri = chrome.galleryUri,
                opacityPercent = chrome.opacityPercent,
                goldBorderPx = 1.5f * density,
                accentBorderPx = 2f * density,
            )
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to draw widget background", e)
            null
        }
    }

    private companion object {
        const val TAG = "WordOfDayWidgetFactory"
    }
}
