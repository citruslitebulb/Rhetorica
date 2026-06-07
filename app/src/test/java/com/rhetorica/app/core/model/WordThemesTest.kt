package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordThemesTest {

    @Test
    fun `canonicalList returns the complete ordered set of themes`() {
        val themes = WordThemes.canonicalList()

        assertEquals(8, themes.size)
        assertEquals(
            listOf(
                "inspirational", "tech", "humanities", "arts",
                "leadership", "democracy", "courage", "legacy"
            ),
            themes
        )
    }

    @Test
    fun `displayName returns friendly names for known themes`() {
        assertEquals("Inspirational", WordThemes.displayName("inspirational"))
        assertEquals("Tech", WordThemes.displayName("tech"))
        assertEquals("Humanities", WordThemes.displayName("humanities"))
        assertEquals("Arts", WordThemes.displayName("arts"))
        assertEquals("Leadership", WordThemes.displayName("leadership"))
        assertEquals("Democracy", WordThemes.displayName("democracy"))
        assertEquals("Courage", WordThemes.displayName("courage"))
        assertEquals("Legacy", WordThemes.displayName("legacy"))
    }

    @Test
    fun `displayName falls back to title case for unknown themes`() {
        assertEquals("Custom", WordThemes.displayName("custom"))
        assertEquals("Foo Bar", WordThemes.displayName("foo bar"))
    }

    @Test
    fun `isValid returns true only for canonical themes`() {
        WordThemes.canonicalList().forEach { theme ->
            assertTrue("Expected $theme to be valid", WordThemes.isValid(theme))
        }

        assertFalse(WordThemes.isValid("unknown"))
        assertFalse(WordThemes.isValid("justice")) // was considered but not included
        assertFalse(WordThemes.isValid(""))
    }
}
