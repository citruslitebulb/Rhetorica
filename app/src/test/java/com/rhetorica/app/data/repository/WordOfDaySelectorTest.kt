package com.rhetorica.app.data.repository

import com.rhetorica.app.core.model.WordComplexity
import com.rhetorica.app.data.local.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WordOfDaySelectorTest {

    private fun word(
        id: Long,
        text: String,
        oratorId: Long,
        complexity: String = "intermediate",
    ) = WordEntity(
        id = id,
        word = text,
        definition = "def $text",
        example = "Example with $text in it.",
        partOfSpeech = "noun",
        oratorId = oratorId,
        complexity = complexity,
    )

    private val library = listOf(
        word(1, "alpha", oratorId = 10),
        word(2, "bravo", oratorId = 10),
        word(3, "charlie", oratorId = 20),
        word(4, "delta", oratorId = 20),
        word(5, "echo", oratorId = 30),
    )

    @Test
    fun `resolveOratorId uses selected orator when not rotating`() {
        assertEquals(20L, WordOfDaySelector.resolveOratorId(selectedOratorId = 20L, rotateThroughAll = false))
        assertNull(WordOfDaySelector.resolveOratorId(selectedOratorId = 20L, rotateThroughAll = true))
        assertNull(WordOfDaySelector.resolveOratorId(selectedOratorId = null, rotateThroughAll = false))
    }

    @Test
    fun `select with orator only uses that orators vocabulary`() {
        val day = 1 // offset 0
        val from20 = WordOfDaySelector.select(library, oratorId = 20L, dayOfYear = day)
        assertEquals(20L, from20?.oratorId)
        assertEquals("charlie", from20?.word)

        val from10 = WordOfDaySelector.select(library, oratorId = 10L, dayOfYear = day)
        assertEquals(10L, from10?.oratorId)
        assertEquals("alpha", from10?.word)

        assertNotEquals(from10?.word, from20?.word)
    }

    @Test
    fun `select without orator uses global pool`() {
        val day = 3 // offset 2 -> charlie in full sorted list
        val global = WordOfDaySelector.select(library, oratorId = null, dayOfYear = day)
        assertEquals("charlie", global?.word)
    }

    @Test
    fun `changing orator changes the word for the same day`() {
        val day = 50
        val a = WordOfDaySelector.select(library, oratorId = 10L, dayOfYear = day)
        val b = WordOfDaySelector.select(library, oratorId = 20L, dayOfYear = day)
        val c = WordOfDaySelector.select(library, oratorId = 30L, dayOfYear = day)
        assertEquals(10L, a?.oratorId)
        assertEquals(20L, b?.oratorId)
        assertEquals(30L, c?.oratorId)
        // Different pools of different sizes yield different headwords for the same calendar day.
        assertNotEquals(a?.id, b?.id)
        assertNotEquals(b?.id, c?.id)
    }

    @Test
    fun `empty orator pool returns null`() {
        assertNull(WordOfDaySelector.select(library, oratorId = 999L, dayOfYear = 1))
    }

    @Test
    fun `selectUnseen skips already shown words until the pool is exhausted`() {
        val first = WordOfDaySelector.selectUnseen(
            allWords = library,
            oratorId = 10L,
            shownIds = emptySet(),
            dayOfYear = 1,
        )
        assertEquals("alpha", first.word?.word)

        val second = WordOfDaySelector.selectUnseen(
            allWords = library,
            oratorId = 10L,
            shownIds = first.shownIds.toSet(),
            dayOfYear = 2,
        )
        assertEquals("bravo", second.word?.word)
        assertEquals(false, second.cycleReset)

        val reset = WordOfDaySelector.selectUnseen(
            allWords = library,
            oratorId = 10L,
            shownIds = second.shownIds.toSet(),
            dayOfYear = 3,
        )
        assertEquals(true, reset.cycleReset)
        assertEquals(1, reset.shownIds.size)
    }

    @Test
    fun `selectUnseen treats a repeated headword from another orator as already shown`() {
        val shared = listOf(
            word(1, "conviction", oratorId = 10),
            word(2, "Conviction", oratorId = 20),
            word(3, "zeal", oratorId = 30),
        )
        val first = WordOfDaySelector.selectUnseen(shared, oratorId = null, shownIds = emptySet(), dayOfYear = 1)
        assertEquals(1L, first.word?.id)

        val second = WordOfDaySelector.selectUnseen(shared, oratorId = null, shownIds = first.shownIds.toSet(), dayOfYear = 2)
        assertEquals("zeal", second.word?.word)
        assertEquals(false, second.cycleReset)

        // Both remaining candidates are now exhausted by headword -> cycle restarts.
        val third = WordOfDaySelector.selectUnseen(shared, oratorId = null, shownIds = second.shownIds.toSet(), dayOfYear = 3)
        assertEquals(true, third.cycleReset)
    }

    @Test
    fun `selectUnseen serves intermediate and advanced words before basic ones`() {
        val mixed = listOf(
            word(1, "king", oratorId = 10, complexity = "basic"),
            word(2, "lamp", oratorId = 10, complexity = "beginner"),
            word(3, "perfidy", oratorId = 10, complexity = "advanced"),
            word(4, "resolve", oratorId = 10, complexity = "intermediate"),
        )
        var shown = emptySet<Long>()
        val order = mutableListOf<String>()
        repeat(4) { day ->
            val pick = WordOfDaySelector.selectUnseen(mixed, oratorId = 10L, shownIds = shown, dayOfYear = day + 1)
            order += pick.word!!.word
            shown = pick.shownIds.toSet()
        }
        assertEquals(setOf("perfidy", "resolve"), order.take(2).toSet())
        assertEquals(setOf("king", "lamp"), order.drop(2).toSet())
    }

    @Test
    fun `favorites limit the global pool`() {
        val pick = WordOfDaySelector.select(
            allWords = library,
            oratorId = null,
            dayOfYear = 1,
            favoriteOratorIds = listOf(30L),
        )
        assertEquals("echo", pick?.word)
    }

    @Test
    fun `catalog visibility hides fictional orators from the pool`() {
        val pick = WordOfDaySelector.select(
            allWords = library,
            oratorId = null,
            dayOfYear = 1,
            visibleOratorIds = listOf(10L, 20L),
        )
        assertEquals("alpha", pick?.word)
        assertNull(
            WordOfDaySelector.select(
                allWords = library,
                oratorId = 30L,
                dayOfYear = 1,
                visibleOratorIds = listOf(10L, 20L),
            ),
        )
    }

    @Test
    fun `hidden selected orator falls back to the visible catalog`() {
        assertNull(
            WordOfDaySelector.resolveOratorId(
                selectedOratorId = 30L,
                rotateThroughAll = false,
                visibleOratorIds = listOf(10L, 20L),
            ),
        )
        val pick = WordOfDaySelector.select(
            allWords = library,
            oratorId = null,
            dayOfYear = 1,
            favoriteOratorIds = emptyList(),
            visibleOratorIds = listOf(10L, 20L),
        )
        assertEquals("alpha", pick?.word)
    }

    @Test
    fun `favorites outside the catalog do not leak hidden words`() {
        assertNull(
            WordOfDaySelector.select(
                allWords = library,
                oratorId = null,
                dayOfYear = 1,
                favoriteOratorIds = listOf(30L),
                visibleOratorIds = listOf(10L, 20L),
            ),
        )
    }

    @Test
    fun `needsNewPick when the persisted day is missing or stale`() {
        assertEquals(true, WordOfDaySelector.needsNewPick(todaysWotdId = null, todaysWotdDate = "2026-08-13", today = "2026-08-13"))
        assertEquals(true, WordOfDaySelector.needsNewPick(todaysWotdId = 4L, todaysWotdDate = "2026-08-12", today = "2026-08-13"))
        assertEquals(false, WordOfDaySelector.needsNewPick(todaysWotdId = 4L, todaysWotdDate = "2026-08-13", today = "2026-08-13"))
    }

    @Test
    fun `pool key includes catalog flags`() {
        assertEquals(
            "all|lit:1|fic:0",
            WordOfDaySelector.poolKey(oratorId = null, includeLiterary = true, includeFictional = false),
        )
        assertEquals(
            "orator:10|lit:0|fic:1",
            WordOfDaySelector.poolKey(oratorId = 10L, includeLiterary = false, includeFictional = true),
        )
    }

    @Test
    fun `any level leaves the pool key unchanged`() {
        val untouched = WordOfDaySelector.poolKey(oratorId = null, includeLiterary = true, includeFictional = false)
        val explicit = WordOfDaySelector.poolKey(
            oratorId = null,
            includeLiterary = true,
            includeFictional = false,
            wordComplexity = WordComplexity.All,
        )
        assertEquals("all|lit:1|fic:0", untouched)
        assertEquals(untouched, explicit)
    }

    @Test
    fun `a chosen tier is part of the pool key`() {
        assertEquals(
            "all|lit:1|fic:0|cx:intermediate",
            WordOfDaySelector.poolKey(
                oratorId = null,
                includeLiterary = true,
                includeFictional = false,
                wordComplexity = WordComplexity.Intermediate,
            ),
        )
    }

    @Test
    fun `basic complexity keeps beginner with basic and drops higher tiers`() {
        val mixed = mixedComplexities()
        var shown = emptySet<Long>()
        val order = mutableListOf<String>()
        repeat(2) { day ->
            val pick = WordOfDaySelector.selectUnseen(
                allWords = mixed,
                oratorId = 10L,
                shownIds = shown,
                dayOfYear = day + 1,
                wordComplexity = WordComplexity.Basic,
            )
            order += pick.word!!.word
            assertTrue(pick.word.complexity == "basic" || pick.word.complexity == "beginner")
            shown = pick.shownIds.toSet()
        }
        assertEquals(listOf("king", "lamp"), order)
    }

    @Test
    fun `a tier stays inside its own cycle and does not fall back`() {
        val mixed = mixedComplexities() + word(5, "valor", oratorId = 10, complexity = "intermediate")
        var shown = emptySet<Long>()
        val order = mutableListOf<String>()
        var reset = false
        repeat(3) { day ->
            val pick = WordOfDaySelector.selectUnseen(
                allWords = mixed,
                oratorId = 10L,
                shownIds = shown,
                dayOfYear = day + 1,
                wordComplexity = WordComplexity.Intermediate,
            )
            order += pick.word!!.word
            reset = pick.cycleReset
            shown = pick.shownIds.toSet()
        }
        assertEquals(listOf("resolve", "valor", "resolve"), order)
        assertTrue(reset)
        assertNull(
            WordOfDaySelector.select(
                allWords = library,
                oratorId = 10L,
                dayOfYear = 1,
                wordComplexity = WordComplexity.Advanced,
            ),
        )
    }

    @Test
    fun `explicit any level keeps the untouched cycle`() {
        val mixed = mixedComplexities()
        val untouched = WordOfDaySelector.selectUnseen(
            allWords = mixed,
            oratorId = 10L,
            shownIds = emptySet(),
            dayOfYear = 1,
        )
        val explicit = WordOfDaySelector.selectUnseen(
            allWords = mixed,
            oratorId = 10L,
            shownIds = emptySet(),
            dayOfYear = 1,
            wordComplexity = WordComplexity.All,
        )
        assertEquals(untouched.word?.id, explicit.word?.id)
        assertEquals("perfidy", explicit.word?.word)
    }

    @Test
    fun `intermediate and advanced together stay in one union cycle`() {
        val selection = WordComplexity.of(WordComplexity.Tier.Advanced, WordComplexity.Tier.Intermediate)
        assertEquals(
            "all|lit:1|fic:0|cx:intermediate,advanced",
            WordOfDaySelector.poolKey(
                oratorId = null,
                includeLiterary = true,
                includeFictional = false,
                wordComplexity = selection,
            ),
        )
        val mixed = mixedComplexities()
        var shown = emptySet<Long>()
        val order = mutableListOf<String>()
        repeat(3) { day ->
            val pick = WordOfDaySelector.selectUnseen(
                allWords = mixed,
                oratorId = 10L,
                shownIds = shown,
                dayOfYear = day + 1,
                wordComplexity = selection,
            )
            order += pick.word!!.word
            assertTrue(pick.word.complexity == "intermediate" || pick.word.complexity == "advanced")
            shown = pick.shownIds.toSet()
        }
        assertEquals(listOf("perfidy", "resolve", "perfidy"), order)
    }

    @Test
    fun `clearing every tier matches any level and the historical pool key`() {
        val cleared = WordComplexity.of(WordComplexity.Tier.Basic, WordComplexity.Tier.Advanced)
            .toggle(WordComplexity.Tier.Basic)
            .toggle(WordComplexity.Tier.Advanced)
        assertEquals(WordComplexity.All, cleared)
        val mixed = mixedComplexities()
        assertEquals(
            WordOfDaySelector.pool(mixed, oratorId = 10L).map { it.id },
            WordOfDaySelector.pool(mixed, oratorId = 10L, wordComplexity = cleared).map { it.id },
        )
        assertEquals(
            WordOfDaySelector.poolKey(oratorId = null, includeLiterary = true, includeFictional = false),
            WordOfDaySelector.poolKey(
                oratorId = null,
                includeLiterary = true,
                includeFictional = false,
                wordComplexity = cleared,
            ),
        )
    }

    @Test
    fun `basic plus intermediate includes beginner and defers foundational words`() {
        val selection = WordComplexity.of(WordComplexity.Tier.Basic, WordComplexity.Tier.Intermediate)
        val mixed = mixedComplexities()
        var shown = emptySet<Long>()
        val order = mutableListOf<String>()
        repeat(3) { day ->
            val pick = WordOfDaySelector.selectUnseen(
                allWords = mixed,
                oratorId = 10L,
                shownIds = shown,
                dayOfYear = day + 1,
                wordComplexity = selection,
            )
            order += pick.word!!.word
            shown = pick.shownIds.toSet()
        }
        assertEquals("resolve", order.first())
        assertEquals(setOf("king", "lamp"), order.drop(1).toSet())
        assertFalse(order.contains("perfidy"))
    }

    private fun mixedComplexities() = listOf(
        word(1, "king", oratorId = 10, complexity = "basic"),
        word(2, "lamp", oratorId = 10, complexity = "beginner"),
        word(3, "perfidy", oratorId = 10, complexity = "advanced"),
        word(4, "resolve", oratorId = 10, complexity = "intermediate"),
    )
}
