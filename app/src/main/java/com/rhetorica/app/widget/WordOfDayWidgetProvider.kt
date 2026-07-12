package com.rhetorica.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.rhetorica.app.MainActivity
import com.rhetorica.app.R
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.WordDao
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.WordOfDaySelector
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
    lateinit var wordDao: WordDao

    @Inject
    lateinit var userPreferencesDao: UserPreferencesDao

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // Immediate safe layout so the host never shows a failed/blank state.
        appWidgetIds.forEach { appWidgetId ->
            try {
                appWidgetManager.updateAppWidget(appWidgetId, loadingViews(context))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to push loading layout for $appWidgetId", e)
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
                            Log.e(TAG, "Failed to apply widget update $appWidgetId", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to build widget $appWidgetId", e)
                    mainHandler.post {
                        try {
                            appWidgetManager.updateAppWidget(appWidgetId, errorViews(appContext))
                        } catch (inner: Exception) {
                            Log.e(TAG, "Failed to apply error layout $appWidgetId", inner)
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
            setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            setViewVisibility(R.id.widgetOratorMinimal, View.GONE)
            setViewVisibility(R.id.widgetGoldDivider, View.GONE)
            setViewVisibility(R.id.widgetAttributionRow, View.GONE)
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
            setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            setViewVisibility(R.id.widgetOratorMinimal, View.GONE)
            setViewVisibility(R.id.widgetGoldDivider, View.GONE)
            setViewVisibility(R.id.widgetAttributionRow, View.GONE)
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
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 200)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (heightDp * density).toInt().coerceAtLeast(1)

        val isSmall = heightDp < 100
        val isLarge = heightDp >= 170

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
        val backgroundBitmap = WidgetAppearance.createElegantCardBitmap(
            widthPx = widthPx,
            heightPx = heightPx,
            cornerRadiusPx = cornerRadiusPx,
            fillColorArgb = fillColor,
        )

        val rootPendingIntent = if (content.wordId != null) {
            val deepLink = Uri.parse("rhetorica://word/${content.wordId}")
            val intent = Intent(context, MainActivity::class.java).apply {
                data = deepLink
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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

        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setImageViewBitmap(R.id.widgetBackgroundImage, backgroundBitmap)
            setOnClickPendingIntent(R.id.widgetRoot, rootPendingIntent)

            setTextViewText(R.id.widgetWordText, content.word)
            setTextColor(R.id.widgetWordText, WidgetAppearance.WIDGET_GOLD)

            val minimalOrator = content.oratorName?.let { "— $it" }.orEmpty()
            if (minimalOrator.isNotEmpty()) {
                setTextViewText(R.id.widgetOratorMinimal, minimalOrator)
                setTextColor(R.id.widgetOratorMinimal, WidgetAppearance.WIDGET_TEXT_SECONDARY)
                setViewVisibility(R.id.widgetOratorMinimal, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widgetOratorMinimal, View.GONE)
            }

            if (!isSmall) {
                setTextViewText(R.id.widgetDefinitionText, content.definition)
                setTextColor(R.id.widgetDefinitionText, WidgetAppearance.WIDGET_TEXT_PRIMARY)
                setViewVisibility(R.id.widgetDefinitionText, View.VISIBLE)
            } else {
                setViewVisibility(R.id.widgetDefinitionText, View.GONE)
            }

            if (!isSmall) {
                setViewVisibility(R.id.widgetGoldDivider, View.VISIBLE)
                setViewVisibility(R.id.widgetAttributionRow, View.VISIBLE)
                setTextViewText(
                    R.id.widgetAttributionText,
                    content.oratorName ?: context.getString(R.string.app_name),
                )
                setTextColor(R.id.widgetAttributionText, WidgetAppearance.WIDGET_TEXT_ATTRIBUTION)
            } else {
                setViewVisibility(R.id.widgetGoldDivider, View.GONE)
                setViewVisibility(R.id.widgetAttributionRow, View.GONE)
            }

            if (isLarge && !content.example.isNullOrBlank()) {
                val quote = content.example.let {
                    if (it.length > 110) it.take(107).trimEnd() + "…" else it
                }
                setTextViewText(R.id.widgetQuoteText, quote)
                setTextColor(R.id.widgetQuoteText, WidgetAppearance.WIDGET_TEXT_SECONDARY)
                setViewVisibility(R.id.widgetQuoteText, View.VISIBLE)

                if (!content.speechTitle.isNullOrBlank()) {
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

            if (isLarge) {
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

    private fun loadContent(context: Context): WidgetRemoteState = runBlocking {
        try {
            if (!::wordDao.isInitialized ||
                !::userPreferencesDao.isInitialized ||
                !::dictionaryRepository.isInitialized
            ) {
                Log.w(TAG, "Hilt dependencies not injected yet")
                return@runBlocking WidgetRemoteState(
                    wordId = null,
                    word = context.getString(R.string.widget_error_title),
                    definition = context.getString(R.string.widget_setup_body),
                    oratorName = null,
                    example = null,
                    speechTitle = null,
                    oratorId = null,
                    backgroundColor = WidgetAppearance.WIDGET_CARD_BG,
                    opacityPercent = DEFAULT_OPACITY_PERCENT,
                )
            }

            val preferences = userPreferencesDao.getUserPreferences()
            val selectedOratorId = preferences?.selectedOratorId
            val rotateThroughAll = preferences?.rotateThroughAll ?: false
            val backgroundColor = preferences?.widgetBackgroundColor
                ?: WidgetAppearance.WIDGET_CARD_BG
            val opacityPercent = preferences?.widgetBackgroundOpacityPercent
                ?: DEFAULT_OPACITY_PERCENT

            // Selected orator owns the daily word; do not pull a global word and re-label it.
            val wotdOratorId = WordOfDaySelector.resolveOratorId(
                selectedOratorId = selectedOratorId,
                rotateThroughAll = rotateThroughAll,
            )

            val count = if (wotdOratorId == null) {
                wordDao.wordCount()
            } else {
                wordDao.wordCountByOrator(wotdOratorId)
            }

            if (count == 0) {
                return@runBlocking WidgetRemoteState(
                    wordId = null,
                    word = context.getString(R.string.widget_error_title),
                    definition = context.getString(R.string.widget_loading_vocab_body),
                    oratorName = null,
                    example = null,
                    speechTitle = null,
                    oratorId = null,
                    backgroundColor = backgroundColor,
                    opacityPercent = opacityPercent,
                )
            }

            val offset = WordOfDaySelector.dayOffset(count)
            val word = if (wotdOratorId == null) {
                wordDao.getWordOfTheDay(offset)
            } else {
                wordDao.getWordOfTheDayByOrator(wotdOratorId, offset)
            }

            if (word == null) {
                return@runBlocking WidgetRemoteState(
                    wordId = null,
                    word = context.getString(R.string.widget_error_title),
                    definition = context.getString(R.string.widget_empty_body),
                    oratorName = null,
                    example = null,
                    speechTitle = null,
                    oratorId = null,
                    backgroundColor = backgroundColor,
                    opacityPercent = opacityPercent,
                )
            }

            // Prefer the selected orator's name so the widget never looks like it
            // "swapped" the speaker to match a global word.
            val oratorName = (wotdOratorId ?: word.oratorId)?.let { id ->
                dictionaryRepository.getOratorProfileById(id)?.name
            }

            WidgetRemoteState(
                wordId = word.id,
                word = word.word,
                definition = word.definition,
                oratorName = oratorName,
                example = word.example.takeIf { it.isNotBlank() },
                speechTitle = word.speech ?: word.source,
                oratorId = word.oratorId ?: wotdOratorId,
                backgroundColor = backgroundColor,
                opacityPercent = opacityPercent,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load widget content", e)
            WidgetRemoteState(
                wordId = null,
                word = context.getString(R.string.widget_error_title),
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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

        private val ioExecutor = Executors.newSingleThreadExecutor()
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}

private data class WidgetRemoteState(
    val wordId: Long?,
    val word: String,
    val definition: String,
    val oratorName: String?,
    val example: String?,
    val speechTitle: String?,
    val oratorId: Long?,
    val backgroundColor: Int,
    val opacityPercent: Int,
)
