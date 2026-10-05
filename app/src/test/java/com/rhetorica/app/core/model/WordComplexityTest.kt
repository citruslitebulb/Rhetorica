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
}
