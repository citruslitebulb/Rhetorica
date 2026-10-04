package com.rhetorica.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.rhetorica.app.core.util.AppLog
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.rhetorica.app.MainActivity
import com.rhetorica.app.R
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.orDefault
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.WordRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

/**
 * Home-screen Word of the Day widget.
 *
 * Layout must stay within RemoteViews-allowed view classes (no plain [View]
 * below API 31). DB work runs on a background executor so the host is not blocked.
 */
@AndroidEntryPoint
class WordOfDayWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var wordRepository: WordRepository

    @Inject
    lateinit var userPreferencesDao: UserPreferencesDao

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

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
     * Fired when the user resizes the widget. Re-render so definition / example
     * visibility scales with the new size immediately.
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId), showLoadingFirst = false)
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        showLoadingFirst: Boolean,
    ) {
        if (showLoadingFirst) {
            // Immediate safe layout so the host never shows a failed/blank state.
            appWidgetIds.forEach { appWidgetId ->
                try {
                    appWidgetManager.updateAppWidget(appWidgetId, loadingViews(context))
                } catch (e: Exception) {
                    AppLog.e(TAG, "Failed to push loading layout for $appWidgetId", e)
                }
            }
        }

        val appContext = context.applicationContext
        ioExecutor.execute {
            appWidgetIds.forEach { appWidgetId ->
                try {
                    val remoteViews = createRemoteViews(appContext, appWidgetManager, appWidgetId)
                    mainHandler.post {
                        try {
                            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                        } catch (e: Exception) {
                            AppLog.e(TAG, "Failed to apply widget update $appWidgetId", e)
                        }
                    }
                } catch (e: Exception) {
                    AppLog.e(TAG, "Failed to build widget $appWidgetId", e)
                    mainHandler.post {
                        try {
                            appWidgetManager.updateAppWidget(appWidgetId, errorViews(appContext))
                        } catch (inner: Exception) {
                            AppLog.e(TAG, "Failed to apply error layout $appWidgetId", inner)
                        }
                    }
                }
            }
        }
    }

    private fun loadingViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, context.getString(R.string.widget_default_word))
            setTextViewText(R.id.widgetDefinitionText, context.getString(R.string.widget_loading_body))
            setViewVisibility(R.id.widgetBadge, View.GONE)
            setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            setViewVisibility(R.id.widgetPartOfSpeech, View.GONE)
            setViewVisibility(R.id.widgetQuoteText, View.GONE)
            setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
            setViewVisibility(R.id.widgetBottomBar, View.GONE)
            setOnClickPendingIntent(R.id.widgetRoot, openAppPendingIntent(context, 0))
        }
    }

    private fun errorViews(context: Context): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, context.getString(R.string.widget_error_title))
            setTextViewText(R.id.widgetDefinitionText, context.getString(R.string.widget_error_body))
            setViewVisibility(R.id.widgetBadge, View.GONE)
            setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            setViewVisibility(R.id.widgetPartOfSpeech, View.GONE)
            setViewVisibility(R.id.widgetQuoteText, View.GONE)
            setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
            setViewVisibility(R.id.widgetBottomBar, View.GONE)
            setOnClickPendingIntent(R.id.widgetRoot, openAppPendingIntent(context, 0))
        }
    }

    private fun createRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ): RemoteViews {
        val layout = resolveLayoutSize(appWidgetManager, appWidgetId)
        val density = context.resources.displayMetrics.density
        val widthPx = (layout.widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (layout.heightDp * density).toInt().coerceAtLeast(1)
        val goldBorderPx = 1.5f * density
        val accentBorderPx = 2f * density

        val content = loadContent(context)

        val fillColor = WidgetAppearance.argbColorInt(
            colorValue = content.backgroundColor,
            opacityPercent = content.opacityPercent,
        )
        val cornerRadiusPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            18f,
            context.resources.displayMetrics,
        )
        val backgroundBitmap = WidgetAppearance.createCardBitmap(
            context = context,
            widthPx = widthPx,
            heightPx = heightPx,
            cornerRadiusPx = cornerRadiusPx,
            fillColorArgb = fillColor,
            imageKey = content.imageKey,
            galleryUri = content.galleryUri,
            opacityPercent = content.opacityPercent,
            goldBorderPx = goldBorderPx,
            accentBorderPx = accentBorderPx,
        )

        val rootPendingIntent = if (content.wordId != null) {
            val deepLink = Uri.parse("rhetorica://word/${content.wordId}")
            val intent = Intent(context, MainActivity::class.java).apply {
                data = deepLink
                flags = OPEN_APP_FLAGS
            }
            PendingIntent.getActivity(
                context,
                (content.wordId % Int.MAX_VALUE).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        } else {
            openAppPendingIntent(context, 0)
        }

        val rootPad = (layout.rootPaddingDp * density).toInt()
        val contentPad = (layout.contentPaddingDp * density).toInt()

        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setImageViewBitmap(R.id.widgetBackgroundImage, backgroundBitmap)
            setOnClickPendingIntent(R.id.widgetRoot, rootPendingIntent)
            setViewPadding(R.id.widgetRoot, rootPad, rootPad, rootPad, rootPad)
            setViewPadding(R.id.widgetContent, contentPad, contentPad, contentPad, contentPad)

            setViewVisibility(R.id.widgetBadge, if (layout.showBadge) View.VISIBLE else View.GONE)
            setTextColor(R.id.widgetBadge, WidgetAppearance.WIDGET_GOLD)

            setTextViewTextSize(R.id.widgetWordText, TypedValue.COMPLEX_UNIT_SP, layout.wordTextSp)
            setInt(R.id.widgetWordText, "setMaxLines", layout.wordMaxLines)
            setTextViewText(R.id.widgetWordText, content.word)
            setTextColor(R.id.widgetWordText, WidgetAppearance.WIDGET_GOLD)

            val meta = WidgetLayoutPolicy.metaLine(
                partOfSpeech = content.partOfSpeech,
                oratorName = content.oratorName,
                showOrator = layout.showOrator,
            )
            if (meta != null) {
                setTextViewText(R.id.widgetPartOfSpeech, meta)
                setTextColor(R.id.widgetPartOfSpeech, WidgetAppearance.WIDGET_GOLD)
                setViewVisibility(R.id.widgetPartOfSpeech, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widgetPartOfSpeech, View.GONE)
            }

            if (layout.showDefinition) {
                setTextViewText(R.id.widgetDefinitionText, content.definition)
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

            // Taller cells keep the usage example already stored on the word.
            if (layout.showExample && !content.example.isNullOrBlank()) {
                val exampleText = ellipsizeExample(content.example, layout.exampleMaxChars)
                setTextViewText(R.id.widgetQuoteText, exampleText)
                setTextColor(R.id.widgetQuoteText, WidgetAppearance.WIDGET_TEXT_PRIMARY)
                setInt(R.id.widgetQuoteText, "setMaxLines", layout.exampleMaxLines)
                setViewVisibility(R.id.widgetQuoteText, View.VISIBLE)

                if (layout.showExampleSource && !content.speechTitle.isNullOrBlank()) {
                    setTextViewText(R.id.widgetQuoteSourceText, content.speechTitle)
                    setTextColor(R.id.widgetQuoteSourceText, WidgetAppearance.WIDGET_GOLD_MUTED)
                    setViewVisibility(R.id.widgetQuoteSourceText, View.VISIBLE)
                } else {
                    setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
                }
            } else {
                setViewVisibility(R.id.widgetQuoteText, View.GONE)
                setViewVisibility(R.id.widgetQuoteSourceText, View.GONE)
            }

            if (layout.showBottomBar) {
                setViewVisibility(R.id.widgetBottomBar, View.VISIBLE)
                if (!content.speechTitle.isNullOrBlank() && content.oratorId != null) {
                    setOnClickPendingIntent(
                        R.id.widgetSpeechCta,
                        createSpeechPendingIntent(context, content.oratorId, content.speechTitle),
                    )
                    setViewVisibility(R.id.widgetSpeechCta, View.VISIBLE)
                } else {
                    setViewVisibility(R.id.widgetSpeechCta, View.GONE)
                }
            } else {
                setViewVisibility(R.id.widgetBottomBar, View.GONE)
            }
        }
    }

    private fun resolveLayoutSize(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ): WidgetLayoutSize {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        return WidgetLayoutPolicy.fromHostBounds(
            minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
            minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
            maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0),
            maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
        )
    }

    private fun ellipsizeExample(text: String, maxChars: Int): String {
        if (maxChars <= 0 || text.length <= maxChars) return text
        val cut = maxChars.coerceAtLeast(1)
        return text.take(cut).trimEnd().trimEnd(',', ';', ':', '.', '—', '-') + "…"
    }

    private fun loadContent(context: Context): WidgetRemoteState = runBlocking {
        try {
            if (!::wordRepository.isInitialized ||
                !::userPreferencesDao.isInitialized ||
                !::dictionaryRepository.isInitialized
            ) {
                AppLog.w(TAG, "Hilt dependencies not injected yet")
                return@runBlocking WidgetRemoteState(
                    wordId = null,
                    word = context.getString(R.string.widget_error_title),
                    partOfSpeech = null,
                    definition = context.getString(R.string.widget_setup_body),
                    oratorName = null,
                    example = null,
                    speechTitle = null,
                    oratorId = null,
                    backgroundColor = WidgetAppearance.WIDGET_CARD_BG,
                    opacityPercent = DEFAULT_OPACITY_PERCENT,
                )
            }

            val preferences = userPreferencesDao.getUserPreferences().orDefault()
            val backgroundColor = preferences.widgetBackgroundColor
            val opacityPercent = preferences.widgetBackgroundOpacityPercent
            val imageKey = preferences.widgetBackgroundImageKey
            val galleryUri = preferences.widgetGalleryUri

            val word = wordRepository.getWordOfTheDayForPreferences(
                selectedOratorId = preferences.selectedOratorId,
                rotateThroughAll = preferences.rotateThroughAll,
            )

            if (word == null) {
                return@runBlocking WidgetRemoteState(
                    wordId = null,
                    word = context.getString(R.string.widget_error_title),
                    partOfSpeech = null,
                    definition = context.getString(R.string.widget_empty_body),
                    oratorName = null,
                    example = null,
                    speechTitle = null,
                    oratorId = null,
                    backgroundColor = backgroundColor,
                    opacityPercent = opacityPercent,
                    imageKey = imageKey,
                    galleryUri = galleryUri,
                )
            }

            val oratorName = word.oratorId?.let { id ->
                dictionaryRepository.getOratorProfileById(id)?.name
            }

            WidgetRemoteState(
                wordId = word.id,
                word = word.word,
                partOfSpeech = word.partOfSpeech.takeIf { it.isNotBlank() },
                definition = word.definition,
                oratorName = oratorName,
                example = word.example.takeIf { it.isNotBlank() },
                speechTitle = word.speech ?: word.source,
                oratorId = word.oratorId,
                backgroundColor = backgroundColor,
                opacityPercent = opacityPercent,
                imageKey = imageKey,
                galleryUri = galleryUri,
            )
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to load widget content", e)
            WidgetRemoteState(
                wordId = null,
                word = context.getString(R.string.widget_error_title),
                partOfSpeech = null,
                definition = context.getString(R.string.widget_error_body),
                oratorName = null,
                example = null,
                speechTitle = null,
                oratorId = null,
                backgroundColor = WidgetAppearance.WIDGET_CARD_BG,
                opacityPercent = DEFAULT_OPACITY_PERCENT,
            )
        }
    }

    private fun openAppPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = OPEN_APP_FLAGS
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createSpeechPendingIntent(
        context: Context,
        oratorId: Long,
        speechTitle: String,
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_SPEECH_FROM_WIDGET
            putExtra(EXTRA_ORATOR_ID, oratorId)
            putExtra(EXTRA_SPEECH_TITLE, speechTitle)
            flags = OPEN_APP_FLAGS
        }
        // Unique request code per orator+speech so UPDATE_CURRENT does not clobber siblings.
        val requestCode = 31 * oratorId.hashCode() + speechTitle.hashCode()
        return PendingIntent.getActivity(
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

        private const val DEFAULT_OPACITY_PERCENT = 80

        private const val OPEN_APP_FLAGS =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

        private val ioExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}

private data class WidgetRemoteState(
    val wordId: Long?,
    val word: String,
    val partOfSpeech: String?,
    val definition: String,
    val oratorName: String?,
    val example: String?,
    val speechTitle: String?,
    val oratorId: Long?,
    val backgroundColor: Int,
    val opacityPercent: Int,
    val imageKey: String = WidgetImagePreset.None.key,
    val galleryUri: String = "",
)
