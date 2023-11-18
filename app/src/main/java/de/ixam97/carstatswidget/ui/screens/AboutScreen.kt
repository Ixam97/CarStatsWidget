package de.ixam97.carstatswidget.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.ui.MainViewModel
import de.ixam97.carstatswidget.ui.Screen
import de.ixam97.carstatswidget.ui.theme.LocalCustomColorsPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MainViewModel, navController: NavController) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val globalState by viewModel.globalState.collectAsState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.about_title))
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = {
                    Text(text = "Copyright")
                },
                supportingContent = {
                    Text(text = "©2023 Maximilian Goldschmidt")
                }
            )
            ListItem(
                modifier = Modifier
                    .clickable(
                        enabled = globalState.updateAvailable == true,
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.github_release_url)))
                            context.startActivity(browserIntent)
                        }
                    ),
                headlineContent = { Text("Version: ${globalState.currentVersion}") },
                supportingContent = {
                    Text(
                        text = if (globalState.updateAvailable == null) "" else stringResource(R.string.about_latest_version, globalState.availableVersion?: "?"),
                        color = if (globalState.updateAvailable == true) {
                            MaterialTheme.colorScheme.error
                        } else {
                            LocalCustomColorsPalette.current.greenSuccess
                        }
                    )
                },
                trailingContent = {
                    when (globalState.updateAvailable) {
                        true -> {
                            Icon(Icons.Default.OpenInNew, null)
                        }
                        false -> {
                            TextButton(
                                onClick = {viewModel.checkForUpdates()}
                            ) {
                                Text(stringResource(R.string.about_check_version))
                            }
                        }
                        else -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            )
            ListItem(
                modifier = Modifier
                    .clickable {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.github_url)))
                        context.startActivity(browserIntent)
                    },
                headlineContent = {
                    Text(text = stringResource(R.string.about_github))
                },
                trailingContent = {
                    Icon(Icons.Default.OpenInNew, null)
                }

            )
            ListItem(
                modifier = Modifier.clickable { navController.navigate(Screen.Licenses.route) },
                headlineContent = {
                    Text(text = stringResource(R.string.licenses_title))
                }
            )
        }
    }
}