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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
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
                    .clickable {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.github_url)))
                        context.startActivity(browserIntent)
                    },
                headlineContent = {
                    Text(text = stringResource(R.string.github_repository_label))
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