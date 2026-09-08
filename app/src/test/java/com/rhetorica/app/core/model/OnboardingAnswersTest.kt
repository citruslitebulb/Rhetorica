package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingAnswersTest {

    private val catalog = listOf(
        profile(1, "Cicero", "Ancient Classics"),
        profile(2, "Lincoln", "19th Century Powerhouses"),
        profile(3, "Shakespeare", "Literary Classics"),
        profile(4, "Jobs", "Modern / Contemporary", themes = listOf("tech")),
        profile(5, "Yoda", "Fictional / Mythic"),
        profile(6, "Aurelius", "Historical / Philosophical"),
    )

    @Test
    fun `default families resolve to classical orators only`() {
        val patch = OnboardingAnswers.resolve(
            orators = catalog,
            families = OratorVoiceFamily.defaultSelected,
            themes = emptySet(),
            selectedOratorIds = emptySet(),
        )

        assertEquals(listOf(1L, 6L), patch.favoriteOratorIds)
        assertNull(patch.selectedOratorId)
        assertTrue(patch.rotateThroughAll)
        assertFalse(patch.includeLiteraryOrators)
        assertFalse(patch.includeFictionalOrators)
        assertTrue(patch.selectedThemeCategories.isEmpty())
    }

    @Test
    fun `empty family selection falls back to classical default`() {
        val patch = OnboardingAnswers.resolve(
            orators = catalog,
            families = emptySet(),
            themes = emptySet(),
            selectedOratorIds = emptySet(),
        )
        assertEquals(listOf(1L, 6L), patch.favoriteOratorIds)
    }

    @Test
    fun `single orator pins the feed to that voice`() {
        val patch = OnboardingAnswers.resolve(
            orators = catalog,
            families = setOf(OratorVoiceFamily.Classical),
            themes = emptySet(),
            selectedOratorIds = setOf(1L),
        )
        assertEquals(listOf(1L), patch.favoriteOratorIds)
        assertEquals(1L, patch.selectedOratorId)
        assertFalse(patch.rotateThroughAll)
    }

    @Test
    fun `literary and fictional flags follow the chosen orators`() {
        val patch = OnboardingAnswers.resolve(
            orators = catalog,
            families = setOf(OratorVoiceFamily.Literary, OratorVoiceFamily.Fictional),
            themes = setOf("arts"),
            selectedOratorIds = setOf(3L, 5L),
        )
        assertTrue(patch.includeLiteraryOrators)
        assertTrue(patch.includeFictionalOrators)
        assertEquals(listOf("arts"), patch.selectedThemeCategories)
        assertEquals(listOf(3L, 5L), patch.favoriteOratorIds)
    }

    @Test
    fun `orators outside selected families are dropped`() {
        val patch = OnboardingAnswers.resolve(
            orators = catalog,
            families = setOf(OratorVoiceFamily.Classical),
            themes = emptySet(),
            selectedOratorIds = setOf(1L, 4L, 5L),
        )
        assertEquals(listOf(1L), patch.favoriteOratorIds)
        assertFalse(patch.includeFictionalOrators)
    }

    @Test
    fun `invalid themes are ignored`() {
        val patch = OnboardingAnswers.resolve(
            orators = catalog,
            families = setOf(OratorVoiceFamily.Classical),
            themes = setOf("leadership", "not-a-theme"),
            selectedOratorIds = setOf(1L, 6L),
        )
        assertEquals(listOf("leadership"), patch.selectedThemeCategories)
    }

    private fun profile(
        id: Long,
        name: String,
        category: String,
        themes: List<String> = emptyList(),
    ) = OratorProfile(
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
        themeCategories = themes,
    )
}
