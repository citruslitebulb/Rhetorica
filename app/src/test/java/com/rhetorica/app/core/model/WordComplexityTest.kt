package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordComplexityTest {
    @Test
    fun `unknown storage values fall back to any level`() {
        assertEquals(WordComplexity.All, WordComplexity.fromStorage(null))
        assertEquals(WordComplexity.All, WordComplexity.fromStorage("nope"))
        assertEquals(WordComplexity.All, WordComplexity.fromStorage(""))
        assertEquals(WordComplexity.All, WordComplexity.fromStorage("all"))
        assertEquals(WordComplexity.Basic, WordComplexity.fromStorage("basic"))
        assertEquals(WordComplexity.Intermediate, WordComplexity.fromStorage("intermediate"))
        assertEquals(WordComplexity.Advanced, WordComplexity.fromStorage("advanced"))
    }

    @Test
    fun `any level matches every seed tier including beginner`() {
        listOf("basic", "beginner", "intermediate", "advanced", "unexpected").forEach { value ->
            assertTrue(WordComplexity.All.matches(value))
        }
        assertEquals(1, WordComplexity.includeAllFlag(WordComplexity.All))
        assertEquals(listOf(""), WordComplexity.sqlValues(WordComplexity.All))
    }

    @Test
    fun `basic includes beginner and excludes higher tiers`() {
        assertTrue(WordComplexity.Basic.matches("basic"))
        assertTrue(WordComplexity.Basic.matches("beginner"))
        assertFalse(WordComplexity.Basic.matches("intermediate"))
        assertFalse(WordComplexity.Basic.matches("advanced"))
        assertEquals(WordComplexity.BASIC_SEED_VALUES, WordComplexity.sqlValues(WordComplexity.Basic).toSet())
        assertEquals(0, WordComplexity.includeAllFlag(WordComplexity.Basic))
    }

    @Test
    fun `intermediate and advanced match only their own seed value`() {
        assertTrue(WordComplexity.Intermediate.matches("intermediate"))
        assertFalse(WordComplexity.Intermediate.matches("beginner"))
        assertFalse(WordComplexity.Intermediate.matches("basic"))
        assertFalse(WordComplexity.Intermediate.matches("advanced"))

        assertTrue(WordComplexity.Advanced.matches("advanced"))
        assertFalse(WordComplexity.Advanced.matches("intermediate"))
        assertFalse(WordComplexity.Advanced.matches("basic"))
        assertFalse(WordComplexity.Advanced.matches("beginner"))
        assertEquals(listOf("advanced"), WordComplexity.sqlValues(WordComplexity.Advanced))
    }

    @Test
    fun `tiers toggle independently and the last one off is any level`() {
        val both = WordComplexity.Basic.toggle(WordComplexity.Tier.Intermediate)
        assertEquals(WordComplexity.of(WordComplexity.Tier.Basic, WordComplexity.Tier.Intermediate), both)
        assertEquals(
            WordComplexity.Advanced,
            WordComplexity.All.toggle(WordComplexity.Tier.Advanced),
        )
        assertEquals(WordComplexity.All, WordComplexity.Basic.toggle(WordComplexity.Tier.Basic))
        assertEquals(
            WordComplexity.All,
            both.toggle(WordComplexity.Tier.Basic).toggle(WordComplexity.Tier.Intermediate),
        )
    }

    @Test
    fun `a combination matches the union and keeps beginner with basic`() {
        val selection = WordComplexity.of(WordComplexity.Tier.Intermediate, WordComplexity.Tier.Advanced)
        assertEquals("intermediate,advanced", selection.storageValue)
        assertEquals(selection, WordComplexity.fromStorage("advanced, intermediate"))
        assertTrue(selection.matches("intermediate"))
        assertTrue(selection.matches("advanced"))
        assertFalse(selection.matches("basic"))
        assertFalse(selection.matches("beginner"))
        assertFalse(selection.matches("unexpected"))
        assertEquals(listOf("intermediate", "advanced"), WordComplexity.sqlValues(selection))
        assertEquals(0, WordComplexity.includeAllFlag(selection))

        val withBasic = selection.toggle(WordComplexity.Tier.Basic)
        assertEquals("basic,intermediate,advanced", withBasic.storageValue)
        assertTrue(withBasic.matches("beginner"))
        assertTrue(withBasic.matches("basic"))
        assertFalse(withBasic.isAll)
        assertFalse(withBasic.matches("unexpected"))
        assertEquals(
            listOf("basic", "beginner", "intermediate", "advanced"),
            WordComplexity.sqlValues(withBasic),
        )
    }

    @Test
    fun `any level token or an empty selection stays unrestricted`() {
        assertEquals(WordComplexity.All, WordComplexity.fromStorage("basic,all"))
        assertEquals(WordComplexity.All, WordComplexity.fromStorage("all"))
        assertTrue(WordComplexity.All.matches("unexpected"))
        assertEquals("all", WordComplexity.All.storageValue)
    }
}
