package com.rhetorica.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rhetorica.app.core.navigation.RhetoricaNavHost
import com.rhetorica.app.core.navigation.TopLevelDestination
import com.rhetorica.app.feature.home.FeedbackOverlay

@Composable
fun RhetoricaApp(
    navController: NavHostController,
) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val destinations = TopLevelDestination.entries
    val currentRoute = currentDestination?.route
    val showBottomBar = destinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        val isSelected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = stringResource(destination.labelRes),
                                )
                            },
                            label = { Text(text = stringResource(destination.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            RhetoricaNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            )
            // Letter-guess uses a pinned keyboard along the bottom; keep the chip
            // off those keys by parking it next to the Quest title instead.
            val onQuiz = currentRoute == TopLevelDestination.Quiz.route
            FeedbackOverlay(
                modifier = Modifier
                    .align(if (onQuiz) Alignment.TopEnd else Alignment.BottomEnd)
                    .padding(innerPadding)
                    .padding(
                        end = 16.dp,
                        top = if (onQuiz) 16.dp else 0.dp,
                        bottom = if (onQuiz) 0.dp else 16.dp,
                    ),
            )
        }
    }
}
