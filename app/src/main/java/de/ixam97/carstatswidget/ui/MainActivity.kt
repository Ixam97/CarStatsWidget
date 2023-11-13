package de.ixam97.carstatswidget.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.ixam97.carstatswidget.ui.components.Navigation
import de.ixam97.carstatswidget.ui.theme.CarStatsWidgetTheme

class MainActivity : ComponentActivity() {
    private var navController: NavController? = null
    override fun onResume() {
        super.onResume()
        navController?.navigate(
            route = Screen.Main.route
        ) {
            popUpTo(navController!!.graph.id) {
                inclusive = true
            }
            anim {  }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CarStatsWidgetTheme {

                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                val mainViewModel: MainViewModel = viewModel()

                navController = Navigation(mainViewModel)
            }
        }
    }
}