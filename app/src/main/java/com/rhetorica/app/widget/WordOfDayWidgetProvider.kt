package com.rhetorica.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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
            val remoteViews = createRemoteViews(context)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun createRemoteViews(context: Context): RemoteViews {
        val (word, definition) = runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    val preferences = userPreferencesDao.getUserPreferences()
                    val selectedOratorId = preferences?.selectedOratorId
                    val rotateThroughAll = preferences?.rotateThroughAll ?: false
                    val effectiveOratorId = if (rotateThroughAll) null else selectedOratorId

                    val count = if (effectiveOratorId == null) {
                        wordDao.wordCount()
                    } else {
                        wordDao.wordCountByOrator(effectiveOratorId)
                    }

                    if (count == 0) {
                        Pair("Word of the Day", "Loading...")
                    } else {
                        val dayOfYear = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).dayOfYear
                        val offset = (dayOfYear - 1) % count
                        val word = if (effectiveOratorId == null) {
                            wordDao.getWordOfTheDay(offset)
                        } else {
                            wordDao.getWordOfTheDayByOrator(effectiveOratorId, offset)
                        }
                        if (word != null) {
                            Pair(word.word, word.definition)
                        } else {
                            Pair("Word of the Day", "No words available")
                        }
                    }
                } catch (e: Exception) {
                    Pair("Word of the Day", "Error loading word")
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

        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, word)
            setTextViewText(R.id.widgetDefinitionText, definition)
            setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
        }
    }
}
