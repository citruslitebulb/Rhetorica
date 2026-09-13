package com.rhetorica.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.rhetorica.app.R
import com.rhetorica.app.core.ui.RhetoricaIcons

/**
 * Destinations shown in the bottom navigation bar.
 * Declaration order is tab order: Quiz, Saved, Home, Profile, Speeches.
 */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Quiz(route = "quiz", labelRes = R.string.nav_quiz, icon = Icons.Outlined.Quiz),
    Saved(route = "saved", labelRes = R.string.nav_saved, icon = Icons.Outlined.StarBorder),
    Home(route = "home", labelRes = R.string.nav_home, icon = RhetoricaIcons.Column),
    Profile(route = "profile", labelRes = R.string.nav_profile, icon = Icons.Outlined.Person),
    Speeches(route = "speeches", labelRes = R.string.nav_speeches, icon = Icons.Outlined.MenuBook),
}

/** Non-tab routes used for secondary screens. */
object AppRoutes {
    const val PROFILE = "profile"
    const val ONBOARDING = "onboarding"
    const val SEARCH = "search"
    const val PRIVACY = "privacy"
    const val WORD_DETAIL = "word/{wordId}"
    fun wordDetail(wordId: Long) = "word/$wordId"
}
