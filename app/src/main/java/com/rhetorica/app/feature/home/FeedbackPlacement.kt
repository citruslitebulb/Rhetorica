package com.rhetorica.app.feature.home

/**
 * Where the feedback control is allowed to sit.
 *
 * It is never a floating overlay. A bottom-end chip covers the onboarding
 * Continue button, quest keyboards, and other bottom actions on a narrow
 * phone. Onboarding hides it. Every other screen hosts [FeedbackAction] in
 * the top bar or the title row, clear of bottom bars and system navigation.
 */
enum class FeedbackSurface {
    Onboarding,
    Home,
    Saved,
    Quiz,
    Profile,
    Speeches,
    Search,
    WordDetail,
    IdiomDetail,
    SpeechDetail,
    Privacy,
}

enum class FeedbackAnchor {
    Hidden,
    TopBarAction,
    TitleRowEnd,
}

object FeedbackPlacement {
    fun anchorFor(surface: FeedbackSurface): FeedbackAnchor = when (surface) {
        FeedbackSurface.Onboarding -> FeedbackAnchor.Hidden
        FeedbackSurface.Saved,
        FeedbackSurface.Quiz,
        FeedbackSurface.Speeches,
        -> FeedbackAnchor.TitleRowEnd
        FeedbackSurface.Home,
        FeedbackSurface.Profile,
        FeedbackSurface.Search,
        FeedbackSurface.WordDetail,
        FeedbackSurface.IdiomDetail,
        FeedbackSurface.SpeechDetail,
        FeedbackSurface.Privacy,
        -> FeedbackAnchor.TopBarAction
    }

    fun coversBottomControls(anchor: FeedbackAnchor): Boolean = when (anchor) {
        FeedbackAnchor.Hidden,
        FeedbackAnchor.TopBarAction,
        FeedbackAnchor.TitleRowEnd,
        -> false
    }
}
