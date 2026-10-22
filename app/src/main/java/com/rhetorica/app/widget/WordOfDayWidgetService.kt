package com.rhetorica.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Supplies the word and idiom cards for [WordOfDayWidgetProvider]'s stack. */
@AndroidEntryPoint
class WordOfDayWidgetService : RemoteViewsService() {

    @Inject
    lateinit var contentLoader: WidgetContentLoader

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        val loader = if (::contentLoader.isInitialized) contentLoader else null
        return WordOfDayWidgetFactory(
            context = applicationContext,
            appWidgetId = appWidgetId,
            loader = loader,
        )
    }
}
