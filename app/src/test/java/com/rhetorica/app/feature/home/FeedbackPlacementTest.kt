package com.rhetorica.app.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FeedbackPlacementTest {

    @Test
    fun `feedback never covers bottom controls`() {
        for (surface in FeedbackSurface.entries) {
            val anchor = FeedbackPlacement.anchorFor(surface)
            assertFalse(surface.name, FeedbackPlacement.coversBottomControls(anchor))
        }
    }

    @Test
    fun `onboarding hides feedback so the continue bar stays clear`() {
        assertEquals(
            FeedbackAnchor.Hidden,
            FeedbackPlacement.anchorFor(FeedbackSurface.Onboarding),
        )
    }

    @Test
    fun `feedback stays reachable on every screen after onboarding`() {
        val hidden = FeedbackSurface.entries.filter { surface ->
            FeedbackPlacement.anchorFor(surface) == FeedbackAnchor.Hidden
        }
        assertEquals(listOf(FeedbackSurface.Onboarding), hidden)
    }

    @Test
    fun `screens with a pinned bottom control keep feedback in the title row`() {
        assertEquals(FeedbackAnchor.TitleRowEnd, FeedbackPlacement.anchorFor(FeedbackSurface.Quiz))
        assertEquals(FeedbackAnchor.TitleRowEnd, FeedbackPlacement.anchorFor(FeedbackSurface.Saved))
        assertEquals(FeedbackAnchor.TitleRowEnd, FeedbackPlacement.anchorFor(FeedbackSurface.Speeches))
    }

    @Test
    fun `screens with a top bar host feedback there`() {
        val topBar = listOf(
            FeedbackSurface.Home,
            FeedbackSurface.Profile,
            FeedbackSurface.Search,
            FeedbackSurface.WordDetail,
            FeedbackSurface.IdiomDetail,
            FeedbackSurface.SpeechDetail,
            FeedbackSurface.Privacy,
        )
        for (surface in topBar) {
            assertEquals(surface.name, FeedbackAnchor.TopBarAction, FeedbackPlacement.anchorFor(surface))
        }
    }
}
