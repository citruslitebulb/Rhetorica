package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import com.rhetorica.app.core.util.AppLog
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.rhetorica.app.R
import kotlin.math.roundToInt

data class WidgetColorPreset(
    val colorValue: Int,
    @StringRes val labelRes: Int,
)

object WidgetAppearance {
    // Elegant dark + gold proposal colors (matching the selected design direction)
    const val WIDGET_CARD_BG = 0xFF1C2433.toInt()
    const val WIDGET_GOLD = 0xFFD4AF37.toInt()
    const val WIDGET_GOLD_MUTED = 0xFFB8973A.toInt()
    const val WIDGET_TEXT_PRIMARY = 0xFFF5F0E6.toInt()
    const val WIDGET_TEXT_SECONDARY = 0xFFC8BFA8.toInt()
    const val WIDGET_TEXT_ATTRIBUTION = 0xFFE8DFC8.toInt()

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

    /**
     * Gold-bordered card background for the premium widget design.
     * [fillColorArgb] should already include the user's opacity preference.
     */
    fun createElegantCardBitmap(
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
        fillColorArgb: Int = WIDGET_CARD_BG,
        borderColorArgb: Int = WIDGET_GOLD,
        borderWidthPx: Float = 5f,
    ): Bitmap {
        val safeWidth = widthPx.coerceAtLeast(1)
        val safeHeight = heightPx.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColorArgb
            style = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColorArgb
            style = Paint.Style.STROKE
            strokeWidth = borderWidthPx
        }

        val rect = RectF(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat())
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, fillPaint)

        // Gold border inset so the stroke sits inside the bounds.
        val inset = borderWidthPx / 2f
        val borderRect = RectF(
            inset,
            inset,
            safeWidth - inset,
            safeHeight - inset,
        )
        val borderRadius = (cornerRadiusPx - inset).coerceAtLeast(0f)
        canvas.drawRoundRect(borderRect, borderRadius, borderRadius, borderPaint)

        return bitmap
    }

    fun createCardBitmap(
        context: Context,
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
        fillColorArgb: Int,
        imageKey: String,
        galleryUri: String,
        opacityPercent: Int,
    ): Bitmap {
        val preset = WidgetImagePreset.fromKey(imageKey)
        val base = when {
            preset == WidgetImagePreset.Gallery && galleryUri.isNotBlank() -> {
                decodeGalleryBitmap(context, galleryUri, widthPx, heightPx, cornerRadiusPx)
                    ?: createElegantCardBitmap(widthPx, heightPx, cornerRadiusPx, fillColorArgb)
            }
            preset != WidgetImagePreset.None && preset != WidgetImagePreset.Gallery -> {
                createTextureBitmap(widthPx, heightPx, cornerRadiusPx, preset, fillColorArgb)
            }
            else -> createElegantCardBitmap(widthPx, heightPx, cornerRadiusPx, fillColorArgb)
        }
        if (preset == WidgetImagePreset.None) return base
        return applyOpacityAndBorder(base, cornerRadiusPx, opacityPercent)
    }

    private fun applyOpacityAndBorder(
        source: Bitmap,
        cornerRadiusPx: Float,
        opacityPercent: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = ((opacityPercent.coerceIn(20, 100) / 100f) * 255).toInt()
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WIDGET_GOLD
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val inset = 2.5f
        canvas.drawRoundRect(
            RectF(inset, inset, source.width - inset, source.height - inset),
            (cornerRadiusPx - inset).coerceAtLeast(0f),
            (cornerRadiusPx - inset).coerceAtLeast(0f),
            borderPaint,
        )
        return bitmap
    }

    private fun createTextureBitmap(
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
        preset: WidgetImagePreset,
        fillColorArgb: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when (preset) {
                WidgetImagePreset.Parchment -> 0xFFE8D9B8.toInt()
                WidgetImagePreset.Marble -> 0xFFD7DDE4.toInt()
                WidgetImagePreset.Midnight -> 0xFF101828.toInt()
                WidgetImagePreset.Velvet -> 0xFF3B1220.toInt()
                else -> fillColorArgb
            }
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()),
            cornerRadiusPx,
            cornerRadiusPx,
            fill,
        )
        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = when (preset) {
                WidgetImagePreset.Parchment -> 0x66B8973A
                WidgetImagePreset.Marble -> 0x55FFFFFF
                WidgetImagePreset.Midnight -> 0x44D4AF37
                else -> 0x55D4AF37
            }
        }
        var y = 12f
        while (y < heightPx) {
            canvas.drawLine(0f, y, widthPx.toFloat(), y + 8f, accent)
            y += 18f
        }
        return createElegantCardBitmap(
            widthPx = widthPx,
            heightPx = heightPx,
            cornerRadiusPx = cornerRadiusPx,
            fillColorArgb = fill.color,
        ).also { elegant ->
            val overlay = Canvas(elegant)
            overlay.drawBitmap(bitmap, 0f, 0f, Paint().apply { alpha = 180 })
        }
    }

    private fun decodeGalleryBitmap(
        context: Context,
        uriString: String,
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
    ): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val targetW = widthPx.coerceAtLeast(1)
            val targetH = heightPx.coerceAtLeast(1)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = BitmapSampling.inSampleSize(
                    srcWidth = bounds.outWidth,
                    srcHeight = bounds.outHeight,
                    reqWidth = targetW,
                    reqHeight = targetH,
                )
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null
            val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
            if (scaled != decoded) decoded.recycle()
            clipToRoundedRect(scaled, cornerRadiusPx)
        } catch (e: Exception) {
            AppLog.e("WidgetAppearance", "Failed to decode gallery image", e)
            null
        }
    }

    private fun clipToRoundedRect(source: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)
        if (output != source) source.recycle()
        return output
    }

    // Legacy solid rounded background (kept for compatibility / old customization path)
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
