package com.rhetorica.app.data.repository

import com.rhetorica.app.core.model.WordComplexity
import com.rhetorica.app.data.local.IdiomEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdiomOfDaySelectorTest {
    @Test
    fun `the same day and filter always pick the same idiom`() {
        val idioms = sample()
        val first = IdiomOfDaySelector.select(idioms, WordComplexity.All, dayOfYear = 40)
        val second = IdiomOfDaySelector.select(idioms, WordComplexity.All, dayOfYear = 40)
        assertEquals(first?.id, second?.id)
        assertEquals(first?.id, IdiomOfDaySelector.select(idioms, WordComplexity.All, dayOfYear = 40 + idioms.size)?.id)
    }

    @Test
    fun `consecutive days move through the pool in id order`() {
        val idioms = sample().shuffled()
        val sorted = idioms.sortedBy { it.id }
        sorted.indices.forEach { offset ->
            val pick = IdiomOfDaySelector.select(idioms, WordComplexity.All, dayOfYear = offset + 1)
            assertEquals(sorted[offset].id, pick?.id)
        }
    }

    @Test
    fun `basic includes beginner and excludes the other tiers`() {
        val idioms = sample()
        val basic = WordComplexity.Basic
        val picks = (1..6).map { day ->
            IdiomOfDaySelector.select(idioms, basic, dayOfYear = day)
        }
        assertTrue(picks.all { it != null && basic.matches(it.complexity) })
        assertEquals(listOf(1L, 2L, 1L), picks.take(3).map { it?.id })
        assertEquals(4L, IdiomOfDaySelector.select(idioms, WordComplexity.Advanced, dayOfYear = 1)?.id)
        assertNull(IdiomOfDaySelector.select(listOf(idiom(3, "intermediate")), WordComplexity.Advanced, dayOfYear = 1))
    }

    @Test
    fun `a comma separated selection is the union of those tiers`() {
        val mixed = WordComplexity.of(WordComplexity.Tier.Basic, WordComplexity.Tier.Advanced)
        val picks = (1..4).map { day ->
            IdiomOfDaySelector.select(sample(), mixed, dayOfYear = day)?.id
        }
        assertEquals(listOf(1L, 2L, 4L, 1L), picks)
    }

    @Test
    fun `hiding the card returns nothing`() {
        assertNull(IdiomOfDaySelector.visible(show = false, idioms = sample(), dayOfYear = 1))
        assertEquals(1L, IdiomOfDaySelector.visible(show = true, idioms = sample(), dayOfYear = 1)?.id)
    }

    @Test
    fun `an empty pool picks nothing`() {
        assertNull(IdiomOfDaySelector.select(emptyList(), WordComplexity.All, dayOfYear = 12))
    }

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
