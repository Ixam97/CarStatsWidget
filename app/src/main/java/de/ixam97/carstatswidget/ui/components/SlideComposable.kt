import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val TIME_DURATION = 200

val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    )
}

val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    )
}

val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    )
}

val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = TIME_DURATION, easing = LinearOutSlowInEasing)
    )
}

@ExperimentalAnimationApi
fun NavGraphBuilder.slideComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    isRoot: Boolean = false,
    content:
    @Composable()
    (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route,
        arguments = arguments,
        enterTransition = if (!isRoot) enterTransition else null,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        deepLinks = deepLinks,
        content = content
    )
}