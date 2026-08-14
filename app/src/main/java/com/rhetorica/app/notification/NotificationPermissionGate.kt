package com.rhetorica.app.notification

/**
 * When to show the POST_NOTIFICATIONS prompt for users who already finished
 * onboarding (upgrades). New users are prompted on the onboarding habit page.
 */
object NotificationPermissionGate {
    const val POST_NOTIFICATIONS_SDK = 33

    fun shouldRequest(
        onboardingCompleted: Boolean,
        notificationsEnabled: Boolean,
        sdkInt: Int,
        permissionGranted: Boolean,
        alreadyAskedThisProcess: Boolean,
    ): Boolean {
        if (!onboardingCompleted) return false
        if (!notificationsEnabled) return false
        if (sdkInt < POST_NOTIFICATIONS_SDK) return false
        if (permissionGranted) return false
        if (alreadyAskedThisProcess) return false
        return true
    }
}
