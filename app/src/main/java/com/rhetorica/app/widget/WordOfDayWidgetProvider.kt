package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.rhetorica.app.R
import com.rhetorica.app.data.local.RhetoricaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class WordOfDayWidgetProvider : AppWidgetProvider() {
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
                    val database = RhetoricaDatabase.getDatabase(context)
                    val wordDao = database.wordDao()
                    val count = wordDao.wordCount()
                    if (count == 0) {
                        Pair("Word of the Day", "Loading...")
                    } else {
                        val dayOfYear = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).dayOfYear
                        val offset = (dayOfYear - 1) % count
                        val word = wordDao.getWordOfTheDay(offset)
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

        return RemoteViews(context.packageName, R.layout.widget_word_of_day).apply {
            setTextViewText(R.id.widgetWordText, word)
            setTextViewText(R.id.widgetDefinitionText, definition)
        }
    }
}
