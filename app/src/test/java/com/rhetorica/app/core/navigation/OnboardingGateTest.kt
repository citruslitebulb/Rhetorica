package com.rhetorica.app.core.navigation

import com.rhetorica.app.data.local.UserPreferencesEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingGateTest {

    @Test
    fun `missing preferences row needs onboarding`() {
        assertTrue(OnboardingGate.needsOnboarding(null))
    }

    @Test
    fun `incomplete onboarding needs onboarding`() {
        val prefs = UserPreferencesEntity.defaults().copy(onboardingCompleted = false)
        assertTrue(OnboardingGate.needsOnboarding(prefs))
    }

    @Test
    fun `completed onboarding does not need onboarding`() {
        val prefs = UserPreferencesEntity.defaults().copy(onboardingCompleted = true)
        assertFalse(OnboardingGate.needsOnboarding(prefs))
    }
}
