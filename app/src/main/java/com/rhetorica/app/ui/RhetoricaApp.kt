package com.rhetorica.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rhetorica.app.core.navigation.RhetoricaNavHost
import com.rhetorica.app.core.navigation.TopLevelDestination

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
                        val isHome = destination == TopLevelDestination.Home
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
                                val glyph = @Composable {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.labelRes),
                                    )
                                }
                                if (isHome) HomeRing(isSelected = isSelected, glyph = glyph) else glyph()
                            },
                            label = {
                                Text(
                                    text = stringResource(destination.labelRes),
                                    fontWeight = if (isHome) FontWeight.SemiBold else null,
                                )
                            },
                            colors = if (isHome) homeItemColors() else NavigationBarItemDefaults.colors(),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // Feedback is hosted by each screen's top bar or title row, not overlaid.
        RhetoricaNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

/**
 * Gold ring that marks Home as the anchor of the app. It replaces the standard
 * pill indicator so the emphasis reads the same whether or not Home is selected.
 */
@Composable
private fun HomeRing(isSelected: Boolean, glyph: @Composable () -> Unit) {
    val gold = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = if (isSelected) gold.copy(alpha = 0.16f) else Color.Transparent,
                shape = CircleShape,
            )
            .border(1.dp, gold.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center,
        content = { glyph() },
    )
}

@Composable
private fun homeItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = Color.Transparent,
)
