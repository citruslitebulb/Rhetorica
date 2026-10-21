package com.rhetorica.app.widget

import android.content.Context
import com.rhetorica.app.R
import com.rhetorica.app.core.model.WordComplexity
import com.rhetorica.app.core.util.AppLog
import com.rhetorica.app.data.local.UserPreferencesDao
import com.rhetorica.app.data.local.orDefault
import com.rhetorica.app.data.repository.DictionaryRepository
import com.rhetorica.app.data.repository.IdiomRepository
import com.rhetorica.app.data.repository.WordRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetChrome(
    val backgroundColor: Int,
    val opacityPercent: Int,
    val imageKey: String,
    val galleryUri: String,
)

data class WidgetSnapshot(
    val cards: List<WidgetDeckCard>,
    val chrome: WidgetChrome,
) {
    val showsFlipControls: Boolean get() = WidgetDeck.showsFlipControls(cards)
}

/**
 * Loads the word and idiom the widget paints. The provider uses the card count
 * to show or hide the arrow row, and the factory binds one RemoteViews per card.
 */
@Singleton
class WidgetContentLoader @Inject constructor(
    private val wordRepository: WordRepository,
    private val idiomRepository: IdiomRepository,
    private val userPreferencesDao: UserPreferencesDao,
    private val dictionaryRepository: DictionaryRepository,
) {
    suspend fun load(context: Context): WidgetSnapshot {
        val wordTitle = context.getString(R.string.widget_card_word_title)
        val idiomTitle = context.getString(R.string.widget_card_idiom_title)
        return try {
            val preferences = userPreferencesDao.getUserPreferences().orDefault()
            val chrome = WidgetChrome(
                backgroundColor = preferences.widgetBackgroundColor,
                opacityPercent = preferences.widgetBackgroundOpacityPercent,
                imageKey = preferences.widgetBackgroundImageKey,
                galleryUri = preferences.widgetGalleryUri,
            )
            val word = wordRepository.getWordOfTheDayForPreferences(
                selectedOratorId = preferences.selectedOratorId,
                rotateThroughAll = preferences.rotateThroughAll,
            )
            if (word == null) {
                return unavailable(
                    context = context,
                    body = context.getString(R.string.widget_empty_body),
                    wordTitle = wordTitle,
                    idiomTitle = idiomTitle,
                    chrome = chrome,
                )
            }
            val oratorName = word.oratorId?.let { id ->
                dictionaryRepository.getOratorProfileById(id)?.name
            }
            val cards = WidgetDeck.cards(
                word = WidgetWordInput(
                    id = word.id,
                    word = word.word,
                    partOfSpeech = word.partOfSpeech.takeIf { it.isNotBlank() },
                    definition = word.definition,
                    oratorName = oratorName,
                    example = word.example.takeIf { it.isNotBlank() },
                    speechTitle = word.speech ?: word.source,
                    oratorId = word.oratorId,
                ),
                idioms = idiomRepository.getAll(),
                showIdiomOfDay = preferences.showIdiomOfDay,
                complexity = WordComplexity.fromStorage(preferences.wordComplexity),
                dayOfYear = LocalDate.now(ZoneId.systemDefault()).dayOfYear,
                wordTitle = wordTitle,
                idiomTitle = idiomTitle,
            )
            WidgetSnapshot(cards = cards, chrome = chrome)
        } catch (e: Exception) {
            AppLog.e(TAG, "Failed to load widget content", e)
            unavailable(
                context = context,
                body = context.getString(R.string.widget_error_body),
                wordTitle = wordTitle,
                idiomTitle = idiomTitle,
                chrome = defaultChrome(),
            )
        }
    }

    companion object {
        fun unavailable(
            context: Context,
            body: String = context.getString(R.string.widget_setup_body),
            wordTitle: String = context.getString(R.string.widget_card_word_title),
            idiomTitle: String = context.getString(R.string.widget_card_idiom_title),
            chrome: WidgetChrome = defaultChrome(),
        ): WidgetSnapshot {
            return WidgetSnapshot(
                cards = WidgetDeck.cards(
                    word = WidgetWordInput(
                        id = null,
                        word = context.getString(R.string.widget_error_title),
                        partOfSpeech = null,
                        definition = body,
                        oratorName = null,
                        example = null,
                        speechTitle = null,
                        oratorId = null,
                    ),
                    idioms = emptyList(),
                    showIdiomOfDay = false,
                    complexity = WordComplexity.All,
                    dayOfYear = 1,
                    wordTitle = wordTitle,
                    idiomTitle = idiomTitle,
                ),
                chrome = chrome,
            )
        }

        fun defaultChrome(): WidgetChrome = WidgetChrome(
            backgroundColor = WidgetAppearance.WIDGET_CARD_BG,
            opacityPercent = 80,
            imageKey = WidgetImagePreset.None.key,
            galleryUri = "",
        )

        private const val TAG = "WidgetContentLoader"
    }
}
