package com.rhetorica.app.widget

import android.content.Context

/**
 * Which card a placed widget is showing.
 *
 * The index is stored per appWidgetId. Rebuilding the widget, including the
 * midnight Today's Word refresh, reads it back instead of jumping to the word.
 * [advance] wraps in both directions.
 */
internal object WidgetDeckIndex {
    fun clamp(index: Int, count: Int): Int {
        if (count <= 0) return 0
        if (index < 0 || index >= count) return 0
        return index
    }

    fun advance(index: Int, count: Int, next: Boolean): Int {
        if (count <= 1) return 0
        val current = clamp(index, count)
        val step = if (next) 1 else -1
        val shifted = current + step
        val wrapped = shifted % count
        return if (wrapped < 0) wrapped + count else wrapped
    }
}

/**
 * Request codes for widget clicks. PendingIntent identity uses the code
 * together with the intent, so each action on each widget gets its own slot.
 */
internal object WidgetPendingIntentIds {
    private const val BASE = 80_000
    private const val STRIDE = 4

    fun card(appWidgetId: Int): Int = slot(appWidgetId, 0)

    fun speech(appWidgetId: Int): Int = slot(appWidgetId, 1)

    fun previous(appWidgetId: Int): Int = slot(appWidgetId, 2)

    fun next(appWidgetId: Int): Int = slot(appWidgetId, 3)

    private fun slot(appWidgetId: Int, offset: Int): Int {
        return BASE + appWidgetId.coerceAtLeast(0) * STRIDE + offset
    }
}

internal class WidgetDeckIndexStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(appWidgetId: Int): Int = prefs.getInt(key(appWidgetId), 0)

    fun set(appWidgetId: Int, index: Int) {
        prefs.edit().putInt(key(appWidgetId), index).commit()
    }

    fun clear(appWidgetId: Int) {
        prefs.edit().remove(key(appWidgetId)).commit()
    }

    fun clearAll() {
        prefs.edit().clear().commit()
    }

    private fun key(appWidgetId: Int) = "card_index_$appWidgetId"

    private companion object {
        const val PREFS_NAME = "rhetorica_widget_deck"
    }
}
