package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OratorVoiceFamilyTest {

    @Test
    fun `default selection is classical only`() {
        assertEquals(setOf(OratorVoiceFamily.Classical), OratorVoiceFamily.defaultSelected)
    }

    @Test
    fun `category and tags map to families`() {
        assertEquals(
            OratorVoiceFamily.Classical,
            OratorVoiceFamily.from("Ancient Classics", tags = listOf("Philosophy")),
        )
        assertEquals(
            OratorVoiceFamily.Classical,
            OratorVoiceFamily.from("Historical / Philosophical", tags = listOf("Stoicism")),
        )
        assertEquals(
            OratorVoiceFamily.Literary,
            OratorVoiceFamily.from("Literary Classics", tags = listOf("Poetry")),
        )
        assertEquals(
            OratorVoiceFamily.Literary,
            OratorVoiceFamily.from("Modern / Contemporary", tags = listOf("Poetry")),
        )
        assertEquals(
            OratorVoiceFamily.Fictional,
            OratorVoiceFamily.from("Fictional / Mythic", tags = listOf("Wisdom")),
        )
        assertEquals(
            OratorVoiceFamily.Fictional,
            OratorVoiceFamily.from("Fictional / Modern Inspirational"),
        )
        assertEquals(
            OratorVoiceFamily.Technology,
            OratorVoiceFamily.from(
                "Modern / Contemporary",
                tags = listOf("Innovation"),
                themeCategories = listOf("tech", "leadership"),
            ),
        )
        assertEquals(
            OratorVoiceFamily.Justice,
            OratorVoiceFamily.from(
                "20th Century Legends",
                tags = listOf("Justice", "Equality"),
            ),
        )
        assertEquals(
            OratorVoiceFamily.Justice,
            OratorVoiceFamily.from(
                "Modern / Contemporary",
                tags = listOf("Law", "Equality", "Dissent"),
            ),
        )
        assertEquals(
            OratorVoiceFamily.Statesmen,
            OratorVoiceFamily.from(
                "19th Century Powerhouses",
                tags = listOf("Leadership", "Equality", "Freedom"),
            ),
        )
    }

    @Test
    fun `fictional category wins over justice tags`() {
        assertEquals(
            OratorVoiceFamily.Fictional,
            OratorVoiceFamily.from(
                "Fictional / Modern Inspirational",
                tags = listOf("Justice", "Empathy"),
            ),
        )
    }

    @Test
    fun `filterByFamilies keeps only selected families`() {
        val orators = listOf(
            profile(1, "Cicero", "Ancient Classics"),
            profile(2, "Lincoln", "19th Century Powerhouses"),
            profile(3, "Yoda", "Fictional / Mythic"),
        )
        val filtered = orators.filter { OratorVoiceFamily.from(it) in setOf(OratorVoiceFamily.Classical) }
        assertEquals(listOf(1L), filtered.map { it.id })
        assertTrue(
            orators.none {
                OratorVoiceFamily.from(it) == OratorVoiceFamily.Classical && it.id == 2L
            },
        )
    }

    private fun profile(id: Long, name: String, category: String) = OratorProfile(
        id = id,
        name = name,
        category = category,
        era = "",
        bio = "",
        portraitUrl = "",
        primaryStyle = "",
        voiceStyle = "",
        colorAccent = 0L,
        sampleSpeech = "",
        tags = emptyList(),
    )
}
