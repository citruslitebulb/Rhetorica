package com.vocabdaily.app.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.vocabdaily.app.R

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(route = "home", labelRes = R.string.nav_home, icon = Icons.Outlined.Home),
    Saved(route = "saved", labelRes = R.string.nav_saved, icon = Icons.Outlined.StarBorder),
    Quiz(route = "quiz", labelRes = R.string.nav_quiz, icon = Icons.Outlined.Quiz),
    Profile(route = "profile", labelRes = R.string.nav_profile, icon = Icons.Outlined.Person),
}
