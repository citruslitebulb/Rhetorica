package com.rhetorica.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.widget.RemoteViews
import com.rhetorica.app.MainActivity
import com.rhetorica.app.R
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.WordDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WordOfDayWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var wordDao: WordDao

    @Inject
    lateinit var userPreferencesDao: UserPreferencesDao

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val remoteViews = createRemoteViews(context, appWidgetManager, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun createRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ): RemoteViews {
        var backgroundColor = 0xFF2C3E50.toInt()
        var opacityPercent = 80

        val content = runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    val preferences = userPreferencesDao.getUserPreferences()
                    val selectedOratorId = preferences?.selectedOratorId
                    val rotateThroughAll = preferences?.rotateThroughAll ?: false
                    backgroundColor = preferences?.widgetBackgroundColor ?: 0xFF2C3E50.toInt()
                    opacityPercent = preferences?.widgetBackgroundOpacityPercent ?: 80
                    val effectiveOratorId = if (rotateThroughAll) null else selectedOratorId

                    val count = if (effectiveOratorId == null) {
                        wordDao.wordCount()
                    } else {
                        wordDao.wordCountByOrator(effectiveOratorId)
                    }

                    if (count == 0) {
                        WidgetRemoteState(
                            word = "Word of the Day",
                            definition = "Loading...",
                            backgroundColor = backgroundColor,
                            opacityPercent = opacityPercent,
                        )
                    } else {
                        val dayOfYear = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).dayOfYear
                        val offset = (dayOfYear - 1) % count
                        val word = if (effectiveOratorId == null) {
                            wordDao.getWordOfTheDay(offset)
                        } else {
                            wordDao.getWordOfTheDayByOrator(effectiveOratorId, offset)
                        }
                        if (word != null) {
                            WidgetRemoteState(
                                word = word.word,
                                definition = word.definition,
                                backgroundColor = backgroundColor,
                                opacityPercent = opacityPercent,
                            )
                        } else {
                            WidgetRemoteState(
                                word = "Word of the Day",
                                definition = "No words available",
                                backgroundColor = backgroundColor,
                                opacityPercent = opacityPercent,
                            )
                        }
                    }
                } catch (e: Exception) {
                    WidgetRemoteState(
                        word = "Word of the Day",
                        definition = "Error loading word",
                        backgroundColor = backgroundColor,
                        opacityPercent = opacityPercent,
                    )
                }
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 200)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 150)
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()

        val backgroundBitmap = WidgetAppearance.createRoundedBackgroundBitmap(
            widthPx = widthPx,
            heightPx = heightPx,
            colorInt = WidgetAppearance.argbColorInt(
                colorValue = content.backgroundColor,
                opacityPercent = content.opacityPercent,
            ),
            cornerRadiusPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                18f,
                context.resources.displayMetrics,
            ),
        )

        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, content.word)
            setTextViewText(R.id.widgetDefinitionText, content.definition)
            setImageViewBitmap(R.id.widgetBackgroundImage, backgroundBitmap)
            setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
        }
    }
}

private data class WidgetRemoteState(
    val word: String,
    val definition: String,
    val backgroundColor: Int,
    val opacityPercent: Int,
)
