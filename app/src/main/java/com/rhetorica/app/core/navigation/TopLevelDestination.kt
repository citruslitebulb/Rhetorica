package com.rhetorica.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.rhetorica.app.R

/**
 * Destinations shown in the bottom navigation bar.
 * Profile/settings is not a tab — open it from Home via the gear icon.
 */
enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(route = "home", labelRes = R.string.nav_home, icon = Icons.Outlined.Home),
    Saved(route = "saved", labelRes = R.string.nav_saved, icon = Icons.Outlined.StarBorder),
    Quiz(route = "quiz", labelRes = R.string.nav_quiz, icon = Icons.Outlined.Quiz),
    Speeches(route = "speeches", labelRes = R.string.nav_speeches, icon = Icons.Outlined.MenuBook),
}

/** Non-tab routes used for secondary screens. */
object AppRoutes {
    const val PROFILE = "profile"
}
