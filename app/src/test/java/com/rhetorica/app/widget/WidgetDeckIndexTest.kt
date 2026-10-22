package com.rhetorica.app.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDeckIndexTest {

    @Test
    fun `next and previous wrap across the deck`() {
        assertEquals(1, WidgetDeckIndex.advance(0, 2, next = true))
        assertEquals(0, WidgetDeckIndex.advance(1, 2, next = true))
        assertEquals(1, WidgetDeckIndex.advance(0, 2, next = false))
        assertEquals(0, WidgetDeckIndex.advance(1, 2, next = false))
    }

    @Test
    fun `wrap works for more than two cards`() {
        assertEquals(0, WidgetDeckIndex.advance(2, 3, next = true))
        assertEquals(2, WidgetDeckIndex.advance(0, 3, next = false))
        assertEquals(1, WidgetDeckIndex.advance(0, 3, next = true))
    }

    @Test
    fun `a single card stays on that card`() {
        assertEquals(0, WidgetDeckIndex.advance(0, 1, next = true))
        assertEquals(0, WidgetDeckIndex.advance(0, 1, next = false))
        assertEquals(0, WidgetDeckIndex.advance(4, 1, next = true))
    }

    @Test
    fun `an empty deck stays at zero`() {
        assertEquals(0, WidgetDeckIndex.clamp(3, 0))
        assertEquals(0, WidgetDeckIndex.advance(1, 0, next = true))
        assertEquals(0, WidgetDeckIndex.advance(1, 0, next = false))
    }

    @Test
    fun `a stored index outside the deck clamps to the word before a step`() {
        assertEquals(0, WidgetDeckIndex.clamp(-1, 2))
        assertEquals(0, WidgetDeckIndex.clamp(9, 2))
        assertEquals(1, WidgetDeckIndex.clamp(1, 2))
        assertEquals(1, WidgetDeckIndex.advance(9, 2, next = true))
        assertEquals(1, WidgetDeckIndex.advance(-4, 2, next = false))
    }

    @Test
    fun `reading the stored index does not advance it`() {
        assertEquals(1, WidgetDeckIndex.clamp(1, 2))
        assertEquals(0, WidgetDeckIndex.clamp(1, 1))
    }

    @Test
    fun `click request codes are unique per widget and action`() {
        val first = codes(7)
        val second = codes(8)
        assertEquals(first.size, first.toSet().size)
        assertEquals(second.size, second.toSet().size)
        assertTrue(first.intersect(second.toSet()).isEmpty())
        assertTrue(WidgetPendingIntentIds.next(1) < WidgetPendingIntentIds.card(2))
    }

    private fun codes(appWidgetId: Int): List<Int> = listOf(
        WidgetPendingIntentIds.card(appWidgetId),
        WidgetPendingIntentIds.speech(appWidgetId),
        WidgetPendingIntentIds.previous(appWidgetId),
        WidgetPendingIntentIds.next(appWidgetId),
    )
}
