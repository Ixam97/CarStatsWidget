package de.ixam97.carstatswidget.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.StateOfChargeWidget
import de.ixam97.carstatswidget.repository.CarDataStatus
import de.ixam97.carstatswidget.ui.theme.CarStatsWidgetTheme
import de.ixam97.carstatswidget.util.intentToString

class WidgetConfigActivity: ComponentActivity() {
    companion object {
        private const val TAG = "WidgetConfigActivity"
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Log.d(TAG, "Intent: ${intentToString(intent)}")

        var widgetGlanceId: GlanceId? = null

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        val manager = GlanceAppWidgetManager(applicationContext)

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        setContent {
            CarStatsWidgetTheme {

                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()
                val viewModel : WidgetConfigViewModel = viewModel()
                val configWidgetState by viewModel.widgetConfigState.collectAsState()

                LaunchedEffect(null) {
                    val glanceIds = manager.getGlanceIds(StateOfChargeWidget::class.java)
                    glanceIds.forEach {
                        if (manager.getAppWidgetId(it) == appWidgetId) {
                            widgetGlanceId = it
                            viewModel.setGlanceId(it)
                            return@forEach
                        }
                    }
                }

                if (configWidgetState.done) {
                    if (!configWidgetState.ready) {
                        finish()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    } else {
                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    }

                }

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                Surface (
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Scaffold (
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(stringResource(R.string.widget_config_title)) }
                            )
                        },
                        floatingActionButton = {
                            if ( configWidgetState.dataStatus == CarDataStatus.NotLoggedIn ) {
                                ExtendedFloatingActionButton(
                                    onClick = { viewModel.clickedDone() },
                                    icon = { Icon(Icons.Default.Login, null) },
                                    text = { Text(stringResource(R.string.widget_config_fab_login)) }
                                )
                            } else if ( configWidgetState.ready ) {
                                ExtendedFloatingActionButton(
                                    onClick = { viewModel.clickedDone() },
                                    icon = { Icon(Icons.Default.Check, null) },
                                    text = { Text(stringResource(R.string.widget_config_fab_Done)) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        if (configWidgetState.ready) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                ListItem(
                                    modifier = Modifier
                                        .clickable { viewModel.changeShowVehicleNames() },
                                    headlineContent = {
                                        Text(stringResource(R.string.widget_config_show_vehicle_name))
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = configWidgetState.showVehicleNames,
                                            onCheckedChange = null,
                                        )
                                    }
                                )
                                Divider()
                                ListItem(
                                    modifier = Modifier
                                        .clickable { viewModel.changeShowLastSeenDates() },
                                    headlineContent = {
                                        Text(stringResource(R.string.widget_config_show_last_seen))
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = configWidgetState.showLastSeenDates,
                                            onCheckedChange = null,
                                        )
                                    }
                                )
                                Divider()
                                ListItem(
                                    modifier = Modifier
                                        .clickable { }
                                        .height(48.dp),
                                    headlineContent = { Text(stringResource(R.string.widget_config_visible_vehicles)) }
                                )

                                if (configWidgetState.vehicleList.isEmpty()) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    configWidgetState.vehicleList.forEach { vehicle ->
                                        CarListItem(
                                            name = vehicle.name,
                                            onClick = { viewModel.changeSelectedVehicleId(id = vehicle.id) },
                                            selected = vehicle.isVisible
                                        )
                                    }
                                }
                                Divider()
                            }
                        } else if (configWidgetState.dataStatus == CarDataStatus.NotLoggedIn) {

                            Card (
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ){
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row (
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Outlined.Info, null)
                                        Text(stringResource(R.string.widget_config_login_reqired))
                                    }
                                    Text(stringResource(R.string.widget_config_login_reqired_text))
                                }
                            }
                        } else if (configWidgetState.dataStatus == CarDataStatus.Unavailable) {
                            Column (
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Column (
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ){
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CarListItem(name: String, onClick: () -> Unit, selected: Boolean = false) {
        ListItem(
            modifier = Modifier
                .clickable { onClick() }
                .height(48.dp),
            headlineContent = {
                Text(name)
            },
            // leadingContent = { Spacer(modifier = Modifier.size(32.dp))},
            leadingContent = {
                Checkbox(
                    modifier = Modifier
                        .padding(start = 32.dp),
                    checked = selected,
                    onCheckedChange = null
                )
            }
        )
    }
}