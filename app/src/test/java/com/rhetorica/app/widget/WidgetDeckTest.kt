package com.rhetorica.app.widget

import com.rhetorica.app.core.model.WordComplexity
import com.rhetorica.app.data.local.IdiomEntity
import com.rhetorica.app.data.repository.IdiomOfDaySelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WidgetDeckTest {

    @Test
    fun `a hidden idiom leaves only the word card`() {
        val cards = deck(showIdiom = false)
        assertEquals(listOf(WidgetCardKind.Word), cards.map { it.kind })
        assertEquals("virtue", cards.single().headline)
        assertFalse(WidgetDeck.showsFlipControls(cards))
    }

    @Test
    fun `the idiom card is the same daily pick and filter as home`() {
        val idioms = sample()
        val complexity = WordComplexity.of(WordComplexity.Tier.Basic, WordComplexity.Tier.Advanced)
        val day = 3
        val cards = deck(idioms = idioms, complexity = complexity, dayOfYear = day)
        val expected = IdiomOfDaySelector.visible(
            show = true,
            idioms = idioms,
            complexity = complexity,
            dayOfYear = day,
        )
        assertEquals(2, cards.size)
        assertTrue(WidgetDeck.showsFlipControls(cards))
        val idiom = cards.single { it.kind == WidgetCardKind.Idiom }
        assertEquals(expected?.id, idiom.targetId)
        assertEquals(expected?.idiom, idiom.headline)
        assertEquals(expected?.meaning, idiom.body)
        assertEquals("Today's idiom", idiom.title)
        assertEquals("rhetorica://idiom/${expected?.id}", WidgetDeck.cardTapUri(idiom))
        assertNull(idiom.example)
        assertNull(idiom.partOfSpeech)
        assertNull(idiom.speechTitle)
        assertNull(WidgetDeck.speechTapUri(idiom))
    }

    @Test
    fun `basic includes beginner idioms`() {
        val cards = deck(complexity = WordComplexity.Basic, dayOfYear = 2)
        assertEquals(2L, cards.single { it.kind == WidgetCardKind.Idiom }.targetId)
    }

    @Test
    fun `a filter with no match keeps the word card only`() {
        val cards = deck(
            idioms = listOf(idiom(3, "intermediate")),
            complexity = WordComplexity.Advanced,
        )
        assertEquals(1, cards.size)
        assertEquals(WidgetCardKind.Word, cards.single().kind)
        assertFalse(WidgetDeck.showsFlipControls(cards))
    }

    @Test
    fun `a missing word does not add an idiom card`() {
        val cards = deck(word = word(id = null), showIdiom = true)
        assertEquals(1, cards.size)
        assertNull(cards.single().targetId)
        assertNull(WidgetDeck.cardTapUri(cards.single()))
        assertFalse(WidgetDeck.showsFlipControls(cards))
    }

    @Test
    fun `the word card keeps the fields the widget already showed`() {
        val card = deck().first()
        assertEquals(WidgetCardKind.Word, card.kind)
        assertEquals("Today's word", card.title)
        assertEquals("virtue", card.headline)
        assertEquals("moral excellence", card.body)
        assertEquals("noun", card.partOfSpeech)
        assertEquals("Cicero", card.oratorName)
        assertEquals("Virtue is its own reward.", card.example)
        assertEquals("On Duties", card.speechTitle)
        assertEquals(7L, card.oratorId)
        assertEquals(42L, card.targetId)
        assertEquals("rhetorica://word/42", WidgetDeck.cardTapUri(card))
        assertEquals(
            "rhetorica://speech/7?title=On%20Duties",
            WidgetDeck.speechTapUri(card),
        )
    }

    @Test
    fun `widget titles match the in-app labels and the receiver name stays`() {
        val strings = File("src/main/res/values/strings.xml").readText()
        assertTrue(strings.contains("<string name=\"widget_card_word_title\">Today\\'s word</string>"))
        assertTrue(strings.contains("<string name=\"widget_card_idiom_title\">Today\\'s idiom</string>"))
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:name=\".widget.WordOfDayWidgetProvider\""))
        assertTrue(manifest.contains("android:name=\".widget.WordOfDayWidgetService\""))
        assertTrue(manifest.contains("android.permission.BIND_REMOTEVIEWS"))
        val deck = File("src/main/res/layout/widget_deck.xml").readText()
        assertTrue(deck.contains("<StackView"))
        assertTrue(deck.contains("android:loopViews=\"true\""))
        assertTrue(deck.contains("@+id/widgetPrev"))
        assertTrue(deck.contains("@+id/widgetNext"))
    }

    private fun deck(
        word: WidgetWordInput = word(),
        idioms: List<IdiomEntity> = sample(),
        showIdiom: Boolean = true,
        complexity: WordComplexity = WordComplexity.All,
        dayOfYear: Int = 1,
    ): List<WidgetDeckCard> {
        return WidgetDeck.cards(
            word = word,
            idioms = idioms,
            showIdiomOfDay = showIdiom,
            complexity = complexity,
            dayOfYear = dayOfYear,
            wordTitle = "Today's word",
            idiomTitle = "Today's idiom",
        )
    }

    private fun word(id: Long? = 42L) = WidgetWordInput(
        id = id,
        word = "virtue",
        partOfSpeech = "noun",
        definition = "moral excellence",
        oratorName = "Cicero",
        example = "Virtue is its own reward.",
        speechTitle = "On Duties",
        oratorId = 7L,
    )

    private fun sample(): List<IdiomEntity> = listOf(
        idiom(1, "basic"),
        idiom(2, "beginner"),
        idiom(3, "intermediate"),
        idiom(4, "advanced"),
    )

    private fun idiom(id: Long, complexity: String) = IdiomEntity(
        id = id,
        idiom = "idiom $id",
        meaning = "meaning $id",
        example = "Example $id.",
        complexity = complexity,
    )
}
