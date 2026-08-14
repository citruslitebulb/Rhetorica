package com.rhetorica.app.core.navigation

import com.rhetorica.app.data.local.UserPreferencesEntity

/**
 * First-run gate. The NavHost graph always starts at Home so bottom-nav
 * `popUpTo(startDestinationId)` stays valid after onboarding is dismissed.
 */
object OnboardingGate {
    fun needsOnboarding(preferences: UserPreferencesEntity?): Boolean {
        return preferences?.onboardingCompleted != true
    }
}
