package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.rhetorica.app.R
import kotlin.math.roundToInt

data class WidgetColorPreset(
    val colorValue: Int,
    @StringRes val labelRes: Int,
)

object WidgetAppearance {
    val colorPresets = listOf(
        WidgetColorPreset(colorValue = 0xFF2C3E50.toInt(), labelRes = R.string.widget_color_midnight),
        WidgetColorPreset(colorValue = 0xFF5C2D2D.toInt(), labelRes = R.string.widget_color_claret),
        WidgetColorPreset(colorValue = 0xFF4A3B18.toInt(), labelRes = R.string.widget_color_bronze),
        WidgetColorPreset(colorValue = 0xFF264653.toInt(), labelRes = R.string.widget_color_ink),
        WidgetColorPreset(colorValue = 0xFF3D405B.toInt(), labelRes = R.string.widget_color_plum),
    )

    fun composeColor(colorValue: Int, opacityPercent: Int): Color {
        val baseColor = Color(colorValue)
        return baseColor.copy(alpha = opacityPercent.coerceIn(20, 100) / 100f)
    }

    fun argbColorInt(colorValue: Int, opacityPercent: Int): Int {
        val alpha = ((opacityPercent.coerceIn(20, 100) / 100f) * 255).roundToInt()
        return (alpha shl 24) or (colorValue and 0x00FFFFFF)
    }

    fun createRoundedBackgroundBitmap(
        widthPx: Int,
        heightPx: Int,
        colorInt: Int,
        cornerRadiusPx: Float,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()),
            cornerRadiusPx,
            cornerRadiusPx,
            paint,
        )
        return bitmap
    }

    fun refreshAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context) ?: return
        val componentName = ComponentName(context, WordOfDayWidgetProvider::class.java)
        val widgetIds = manager.getAppWidgetIds(componentName)
        if (widgetIds.isEmpty()) return

        val intent = Intent(context, WordOfDayWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        }
        context.sendBroadcast(intent)
    }
}
