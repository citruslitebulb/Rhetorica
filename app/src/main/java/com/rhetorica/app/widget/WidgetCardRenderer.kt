package com.rhetorica.app.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.rhetorica.app.MainActivity
import com.rhetorica.app.R

/**
 * Binds one deck card into the item layout. Collection items take fill-in
 * intents so the host template can open the matching detail screen.
 */
object WidgetCardRenderer {
    fun bind(
        context: Context,
        card: WidgetDeckCard,
        layout: WidgetLayoutSize,
        background: Bitmap?,
        showsFlipControls: Boolean,
        packageName: String = context.packageName,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        val rootPad = (layout.rootPaddingDp * density).toInt()
        val contentPad = (layout.contentPaddingDp * density).toInt()
        val flipReserve = if (showsFlipControls) (FLIP_RESERVE_DP * density).toInt() else 0
        val showTitle = card.targetId != null && (layout.showBadge || showsFlipControls)

        return RemoteViews(packageName, R.layout.widget_word_of_day).apply {
            if (background != null) {
                setImageViewBitmap(R.id.widgetBackgroundImage, background)
            }
            setOnClickFillInIntent(R.id.widgetRoot, fillIn(WidgetDeck.cardTapUri(card)))
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
                setOnClickFillInIntent(R.id.widgetSpeechCta, fillIn(speechUri))
            } else {
                setViewVisibility(R.id.widgetBottomBar, View.GONE)
            }
        }
    }

    fun loading(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, context.getString(R.string.widget_default_word))
            setTextViewText(R.id.widgetDefinitionText, context.getString(R.string.widget_loading_body))
            setViewVisibility(R.id.widgetBadge, View.GONE)
            setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            setViewVisibility(R.id.widgetPartOfSpeech, View.GONE)
            setViewVisibility(R.id.widgetQuoteText, View.GONE)
            setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
            setViewVisibility(R.id.widgetBottomBar, View.GONE)
            setOnClickPendingIntent(R.id.widgetRoot, openApp(context))
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
            setOnClickPendingIntent(R.id.widgetRoot, openApp(context))
        }
    }

    private fun fillIn(uri: String?): Intent {
        return Intent().apply {
            if (uri != null) data = Uri.parse(uri)
        }
    }

    private fun openApp(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ellipsizeExample(text: String, maxChars: Int): String {
        if (maxChars <= 0 || text.length <= maxChars) return text
        val cut = maxChars.coerceAtLeast(1)
        return text.take(cut).trimEnd().trimEnd(',', ';', ':', '.', '—', '-') + "…"
    }

    private const val FLIP_RESERVE_DP = 36
}
