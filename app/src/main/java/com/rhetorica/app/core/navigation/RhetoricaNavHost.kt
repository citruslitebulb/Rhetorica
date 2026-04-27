package com.rhetorica.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rhetorica.app.feature.home.HomeRoute
import com.rhetorica.app.feature.profile.ProfileRoute
import com.rhetorica.app.feature.quiz.QuizRoute
import com.rhetorica.app.feature.saved.SavedRoute
import com.rhetorica.app.feature.word.navigateToWordDetail
import com.rhetorica.app.feature.word.wordDetailScreen

@Composable
fun RhetoricaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.Home.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.Home.route) {
            HomeRoute(onWordClick = { wordId -> navController.navigateToWordDetail(wordId) })
        }
        composable(TopLevelDestination.Saved.route) {
            SavedRoute(onWordClick = { wordId -> navController.navigateToWordDetail(wordId) })
        }
        composable(TopLevelDestination.Quiz.route) { QuizRoute() }
        composable(TopLevelDestination.Profile.route) { ProfileRoute() }
        wordDetailScreen(onBack = { navController.popBackStack() })
    }
}
