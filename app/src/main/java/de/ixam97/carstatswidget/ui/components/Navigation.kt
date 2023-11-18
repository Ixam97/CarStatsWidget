package de.ixam97.carstatswidget.ui.components

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.ixam97.carstatswidget.ui.MainViewModel
import de.ixam97.carstatswidget.ui.Screen
import de.ixam97.carstatswidget.ui.screens.AboutScreen
import de.ixam97.carstatswidget.ui.screens.LicensesScreen
import de.ixam97.carstatswidget.ui.screens.MainScreen
import slideComposable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Navigation(mainViewModel: MainViewModel, destination: String? = null): NavController {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController =  navController,
        startDestination = Screen.Main.route,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
        popEnterTransition = { EnterTransition.None},
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
        exitTransition = { ExitTransition.None }
    ) {
            slideComposable(
                route = Screen.Main.route,
                isRoot = true
            ) {
                MainScreen(mainViewModel, navController)
            }
            slideComposable(
                route = Screen.About.route
            ) {
                AboutScreen(mainViewModel, navController)
            }
            slideComposable(
                route = Screen.Licenses.route
            ) {
                LicensesScreen(navController)
            }
    }

    if (destination != null) {
        navController.navigate(destination)
    }
    return navController
}