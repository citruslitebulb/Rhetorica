package com.rhetorica.app.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionGateTest {

    @Test
    fun `upgrade user with notifications on and no permission is prompted`() {
        assertTrue(
            NotificationPermissionGate.shouldRequest(
                onboardingCompleted = true,
                notificationsEnabled = true,
                sdkInt = 33,
                permissionGranted = false,
                alreadyAskedThisProcess = false,
            ),
        )
    }

    @Test
    fun `first-run onboarding is not prompted from the main shell`() {
        assertFalse(
            NotificationPermissionGate.shouldRequest(
                onboardingCompleted = false,
                notificationsEnabled = true,
                sdkInt = 33,
                permissionGranted = false,
                alreadyAskedThisProcess = false,
            ),
        )
    }

    @Test
    fun `disabled notifications or existing grant skip the prompt`() {
        assertFalse(
            NotificationPermissionGate.shouldRequest(
                onboardingCompleted = true,
                notificationsEnabled = false,
                sdkInt = 33,
                permissionGranted = false,
                alreadyAskedThisProcess = false,
            ),
        )
        assertFalse(
            NotificationPermissionGate.shouldRequest(
                onboardingCompleted = true,
                notificationsEnabled = true,
                sdkInt = 33,
                permissionGranted = true,
                alreadyAskedThisProcess = false,
            ),
        )
    }

    @Test
    fun `pre-tiramisu and already-asked skip the prompt`() {
        assertFalse(
            NotificationPermissionGate.shouldRequest(
                onboardingCompleted = true,
                notificationsEnabled = true,
                sdkInt = 32,
                permissionGranted = false,
                alreadyAskedThisProcess = false,
            ),
        )
        assertFalse(
            NotificationPermissionGate.shouldRequest(
                onboardingCompleted = true,
                notificationsEnabled = true,
                sdkInt = 33,
                permissionGranted = false,
                alreadyAskedThisProcess = true,
            ),
        )
    }
}
