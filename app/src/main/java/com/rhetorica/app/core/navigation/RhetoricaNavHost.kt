package com.rhetorica.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rhetorica.app.feature.home.HomeRoute
import com.rhetorica.app.feature.onboarding.OnboardingRoute
import com.rhetorica.app.feature.profile.PrivacyPolicyRoute
import com.rhetorica.app.feature.profile.ProfileRoute
import com.rhetorica.app.feature.quiz.QuizRoute
import com.rhetorica.app.feature.saved.SavedRoute
import com.rhetorica.app.feature.search.SearchRoute
import com.rhetorica.app.feature.speech.SpeechesRoute
import com.rhetorica.app.feature.speech.navigateToFullSpeech
import com.rhetorica.app.feature.speech.speechDetailScreen
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
        composable(AppRoutes.ONBOARDING) {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(TopLevelDestination.Home.route) {
                        popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(TopLevelDestination.Home.route) {
            HomeRoute(
                onWordClick = { wordId -> navController.navigateToWordDetail(wordId) },
                onSearchClick = {
                    navController.navigate(AppRoutes.SEARCH) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(TopLevelDestination.Saved.route) {
            SavedRoute(onWordClick = { wordId -> navController.navigateToWordDetail(wordId) })
        }
        composable(TopLevelDestination.Quiz.route) { QuizRoute() }
        composable(TopLevelDestination.Profile.route) {
            ProfileRoute(
                onPrivacyPolicy = {
                    navController.navigate(AppRoutes.PRIVACY) { launchSingleTop = true }
                },
            )
        }
        composable(TopLevelDestination.Speeches.route) {
            SpeechesRoute(
                onSpeechClick = { oratorId, title -> navController.navigateToFullSpeech(oratorId, title) },
            )
        }
        composable(AppRoutes.SEARCH) {
            SearchRoute(
                onBack = { navController.popBackStack() },
                onWordClick = { wordId -> navController.navigateToWordDetail(wordId) },
                onSpeechClick = { oratorId, title -> navController.navigateToFullSpeech(oratorId, title) },
            )
        }
        composable(AppRoutes.PRIVACY) {
            PrivacyPolicyRoute(onBack = { navController.popBackStack() })
        }
        wordDetailScreen(
            onBack = { navController.popBackStack() },
            onReadFullSpeech = { oratorId, title -> navController.navigateToFullSpeech(oratorId, title) },
        )
        speechDetailScreen(onBack = { navController.popBackStack() })
    }
}
