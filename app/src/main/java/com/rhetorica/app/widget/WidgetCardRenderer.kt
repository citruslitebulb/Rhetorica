package com.rhetorica.app.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.rhetorica.app.R
import com.rhetorica.app.core.util.AppLog

/**
 * Binds the one visible deck card into the widget layout. The background is
 * drawn once, on this card. The other card is not in the hierarchy.
 */
object WidgetCardRenderer {
    fun bind(
        views: RemoteViews,
        context: Context,
        appWidgetId: Int,
        card: WidgetDeckCard,
        layout: WidgetLayoutSize,
        background: Bitmap?,
        showsFlipControls: Boolean,
    ) {
        val density = context.resources.displayMetrics.density
        val rootPad = (layout.rootPaddingDp * density).toInt()
        val contentPad = (layout.contentPaddingDp * density).toInt()
        val flipReserve = if (showsFlipControls) (FLIP_RESERVE_DP * density).toInt() else 0
        val showTitle = card.targetId != null && (layout.showBadge || showsFlipControls)

        views.apply {
            if (background != null) {
                setImageViewBitmap(R.id.widgetBackgroundImage, background)
            }
            setOnClickPendingIntent(
                R.id.widgetContent,
                WidgetClicks.open(
                    context = context,
                    requestCode = WidgetPendingIntentIds.card(appWidgetId),
                    uri = WidgetDeck.cardTapUri(card),
                ),
            )
            setViewPadding(R.id.widgetRoot, rootPad, rootPad, rootPad, rootPad)
            setViewPadding(
                R.id.widgetContent,
                contentPad,
                contentPad,
                contentPad,
                contentPad + flipReserve,
            )

            if (showTitle) {
                setTextViewText(R.id.widgetBadge, card.title)
                setTextColor(R.id.widgetBadge, WidgetAppearance.WIDGET_GOLD)
                setViewVisibility(R.id.widgetBadge, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widgetBadge, View.GONE)
            }

            setTextViewTextSize(R.id.widgetWordText, TypedValue.COMPLEX_UNIT_SP, layout.wordTextSp)
            setInt(R.id.widgetWordText, "setMaxLines", layout.wordMaxLines)
            setTextViewText(R.id.widgetWordText, card.headline)
            setTextColor(R.id.widgetWordText, WidgetAppearance.WIDGET_GOLD)

            val meta = if (card.kind == WidgetCardKind.Word) {
                WidgetLayoutPolicy.metaLine(
                    partOfSpeech = card.partOfSpeech,
                    oratorName = card.oratorName,
                    showOrator = layout.showOrator,
                )
            } else {
                null
            }
            if (meta != null) {
                setTextViewText(R.id.widgetPartOfSpeech, meta)
                setTextColor(R.id.widgetPartOfSpeech, WidgetAppearance.WIDGET_GOLD)
                setViewVisibility(R.id.widgetPartOfSpeech, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widgetPartOfSpeech, View.GONE)
            }

            if (layout.showDefinition) {
                setTextViewText(R.id.widgetDefinitionText, card.body)
                setTextColor(R.id.widgetDefinitionText, WidgetAppearance.WIDGET_TEXT_PRIMARY)
                setTextViewTextSize(
                    R.id.widgetDefinitionText,
                    TypedValue.COMPLEX_UNIT_SP,
                    layout.definitionTextSp,
                )
                setInt(R.id.widgetDefinitionText, "setMaxLines", layout.definitionMaxLines)
                setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widgetDefinitionText, View.GONE)
            }

            val example = card.example
            if (card.kind == WidgetCardKind.Word && layout.showExample && !example.isNullOrBlank()) {
                setTextViewText(R.id.widgetQuoteText, ellipsizeExample(example, layout.exampleMaxChars))
                setTextColor(R.id.widgetQuoteText, WidgetAppearance.WIDGET_TEXT_PRIMARY)
                setInt(R.id.widgetQuoteText, "setMaxLines", layout.exampleMaxLines)
                setViewVisibility(R.id.widgetQuoteText, View.VISIBLE)

                if (layout.showExampleSource && !card.speechTitle.isNullOrBlank()) {
                    setTextViewText(R.id.widgetQuoteSourceText, card.speechTitle)
                    setTextColor(R.id.widgetQuoteSourceText, WidgetAppearance.WIDGET_GOLD_MUTED)
                    setViewVisibility(R.id.widgetQuoteSourceText, View.VISIBLE)
                } else {
                    setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
                }
            } else {
                setViewVisibility(R.id.widgetQuoteText, View.GONE)
                setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
            }

            val speechUri = WidgetDeck.speechTapUri(card)
            if (card.kind == WidgetCardKind.Word && layout.showBottomBar && speechUri != null) {
                setViewVisibility(R.id.widgetBottomBar, View.VISIBLE)
                setViewVisibility(R.id.widgetSpeechCta, View.VISIBLE)
                setOnClickPendingIntent(
                    R.id.widgetSpeechCta,
                    WidgetClicks.open(
                        context = context,
                        requestCode = WidgetPendingIntentIds.speech(appWidgetId),
                        uri = speechUri,
                    ),
                )
            } else {
                setViewVisibility(R.id.widgetBottomBar, View.GONE)
            }
        }
    }

    fun error(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, context.getString(R.string.widget_error_title))
            setTextViewText(R.id.widgetDefinitionText, context.getString(R.string.widget_error_body))
            setViewVisibility(R.id.widgetBadge, View.GONE)
            setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            setViewVisibility(R.id.widgetPartOfSpeech, View.GONE)
            setViewVisibility(R.id.widgetQuoteText, View.GONE)
            setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
            setViewVisibility(R.id.widgetBottomBar, View.GONE)
            setViewVisibility(R.id.widgetFlipBar, View.GONE)
            setOnClickPendingIntent(
                R.id.widgetRoot,
                WidgetClicks.open(context = context, requestCode = OPEN_APP_REQUEST, uri = null),
            )
        }
    }

    fun backgroundBitmap(
        context: Context,
        layout: WidgetLayoutSize,
        chrome: WidgetChrome,
    ): Bitmap? {
        return try {
            val density = context.resources.displayMetrics.density
            val widthPx = (layout.widthDp * density).toInt().coerceAtLeast(1)
            val heightPx = (layout.heightDp * density).toInt().coerceAtLeast(1)
            val cornerRadiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                CARD_CORNER_RADIUS_DP,
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

    private fun ellipsizeExample(text: String, maxChars: Int): String {
        if (maxChars <= 0 || text.length <= maxChars) return text
        val cut = maxChars.coerceAtLeast(1)
        return text.take(cut).trimEnd().trimEnd(',', ';', ':', '.', '—', '-') + "…"
    }

    private const val FLIP_RESERVE_DP = 36
    private const val CARD_CORNER_RADIUS_DP = 18f
    private const val OPEN_APP_REQUEST = 1
    private const val TAG = "WidgetCardRenderer"
}
