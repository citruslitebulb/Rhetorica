package com.rhetorica.app.widget

import com.rhetorica.app.core.model.WordComplexity
import com.rhetorica.app.data.local.IdiomEntity
import com.rhetorica.app.data.repository.IdiomOfDaySelector
import java.net.URLEncoder

/**
 * Which cards the home-screen widget offers, and where a tap on each one goes.
 *
 * Card 1 is today's word. Card 2 is today's idiom when Profile still shows that
 * card and the complexity filter has a match. The idiom is the same daily pick
 * [IdiomOfDaySelector] uses for the Home card.
 */
enum class WidgetCardKind {
    Word,
    Idiom,
}

data class WidgetWordInput(
    val id: Long?,
    val word: String,
    val partOfSpeech: String?,
    val definition: String,
    val oratorName: String?,
    val example: String?,
    val speechTitle: String?,
    val oratorId: Long?,
)

data class WidgetDeckCard(
    val kind: WidgetCardKind,
    val title: String,
    val headline: String,
    val body: String,
    val targetId: Long?,
    val partOfSpeech: String? = null,
    val oratorName: String? = null,
    val example: String? = null,
    val speechTitle: String? = null,
    val oratorId: Long? = null,
) {
    val itemId: Long
        get() = when (kind) {
            WidgetCardKind.Word -> targetId ?: WORD_ITEM_ID
            WidgetCardKind.Idiom -> -(targetId ?: 0L) - 1L
        }

    private companion object {
        const val WORD_ITEM_ID = 0L
    }
}

object WidgetDeck {
    fun cards(
        word: WidgetWordInput,
        idioms: List<IdiomEntity>,
        showIdiomOfDay: Boolean,
        complexity: WordComplexity,
        dayOfYear: Int,
        wordTitle: String,
        idiomTitle: String,
    ): List<WidgetDeckCard> {
        val wordCard = WidgetDeckCard(
            kind = WidgetCardKind.Word,
            title = wordTitle,
            headline = word.word,
            body = word.definition,
            targetId = word.id,
            partOfSpeech = word.partOfSpeech,
            oratorName = word.oratorName,
            example = word.example,
            speechTitle = word.speechTitle,
            oratorId = word.oratorId,
        )
        if (word.id == null) return listOf(wordCard)
        val idiom = IdiomOfDaySelector.visible(
            show = showIdiomOfDay,
            idioms = idioms,
            complexity = complexity,
            dayOfYear = dayOfYear,
        ) ?: return listOf(wordCard)
        return listOf(
            wordCard,
            WidgetDeckCard(
                kind = WidgetCardKind.Idiom,
                title = idiomTitle,
                headline = idiom.idiom,
                body = idiom.meaning,
                targetId = idiom.id,
            ),
        )
    }

    fun showsFlipControls(cards: List<WidgetDeckCard>): Boolean = cards.size > 1

    /** Whole-card tap. Null opens the app without a detail destination. */
    fun cardTapUri(card: WidgetDeckCard): String? {
        val id = card.targetId ?: return null
        return when (card.kind) {
            WidgetCardKind.Word -> "rhetorica://word/$id"
            WidgetCardKind.Idiom -> "rhetorica://idiom/$id"
        }
    }

    /** Speech control on the word card. The idiom card has no speech target. */
    fun speechTapUri(card: WidgetDeckCard): String? {
        if (card.kind != WidgetCardKind.Word) return null
        val oratorId = card.oratorId ?: return null
        val title = card.speechTitle?.takeIf { it.isNotBlank() } ?: return null
        return "rhetorica://speech/$oratorId?title=${encodeQuery(title)}"
    }

    private fun encodeQuery(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
    }
}
