package com.rhetorica.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPoolTest {

    private val orators = listOf(
        profile(1, "Cicero", themes = listOf("humanities", "leadership")),
        profile(2, "Lincoln", themes = listOf("leadership", "democracy")),
        profile(3, "Jobs", themes = listOf("tech", "leadership")),
    )

    @Test
    fun `rotation uses favorites only when rotating`() {
        assertEquals(
            listOf(1L),
            LearningPool.rotationOratorIds(
                visibleOratorIds = listOf(1L, 2L, 3L),
                rotateThroughAll = true,
                favoriteOratorIds = listOf(1L),
            ),
        )
        assertEquals(
            listOf(1L, 2L, 3L),
            LearningPool.rotationOratorIds(
                visibleOratorIds = listOf(1L, 2L, 3L),
                rotateThroughAll = false,
                favoriteOratorIds = listOf(1L),
            ),
        )
    }

    @Test
    fun `favorites outside the visible catalog are dropped`() {
        assertEquals(
            listOf(1L),
            LearningPool.rotationOratorIds(
                visibleOratorIds = listOf(1L, 2L),
                rotateThroughAll = true,
                favoriteOratorIds = listOf(1L, 99L),
            ),
        )
    }

    @Test
    fun `theme filter intersects favorites instead of replacing them`() {
        val scope = LearningPool.feedScope(
            visibleOrators = orators,
            selectedOratorId = null,
            rotateThroughAll = true,
            favoriteOratorIds = listOf(1L, 2L),
            selectedThemes = listOf("leadership"),
        )
        assertNull(scope.feedOratorId)
        assertEquals(listOf(1L, 2L), scope.scopedOratorIds?.toList())
        assertTrue(3L !in scope.scopedOratorIds.orEmpty())
    }

    @Test
    fun `single selected orator is the feed when not rotating`() {
        val scope = LearningPool.feedScope(
            visibleOrators = orators,
            selectedOratorId = 2L,
            rotateThroughAll = false,
            favoriteOratorIds = emptyList(),
            selectedThemes = emptyList(),
        )
        assertEquals(2L, scope.feedOratorId)
        assertNull(scope.scopedOratorIds)
        assertTrue(scope.hasActiveFilters)
    }

    @Test
    fun `empty favorites while rotating uses the full visible catalog`() {
        val scope = LearningPool.feedScope(
            visibleOrators = orators,
            selectedOratorId = null,
            rotateThroughAll = true,
            favoriteOratorIds = emptyList(),
            selectedThemes = emptyList(),
        )
        assertEquals(listOf(1L, 2L, 3L), scope.scopedOratorIds?.toList())
        assertEquals(false, scope.hasActiveFilters)
    }

    private fun profile(
        id: Long,
        name: String,
        themes: List<String>,
    ) = OratorProfile(
        id = id,
        name = name,
        category = "Historical",
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
