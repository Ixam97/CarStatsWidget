package de.ixam97.carstatswidget.ui

sealed class Screen(val route: String) {
    object MainNavigation : Screen("main_navigation")
    object Main : Screen("main_screen")
    object About : Screen("about_screen")
    object Licenses : Screen("licenses_screen")
    object Login : Screen("login_screen")
}