package com.vocabdaily.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vocabdaily.app.feature.home.HomeRoute
import com.vocabdaily.app.feature.profile.ProfileRoute
import com.vocabdaily.app.feature.quiz.QuizRoute
import com.vocabdaily.app.feature.saved.SavedRoute

@Composable
fun VocabDailyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Home.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.Home.route) { HomeRoute() }
        composable(TopLevelDestination.Saved.route) { SavedRoute() }
        composable(TopLevelDestination.Quiz.route) { QuizRoute() }
        composable(TopLevelDestination.Profile.route) { ProfileRoute() }
    }
}
