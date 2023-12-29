package de.ixam97.carstatswidget.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import de.ixam97.carstatswidget.BuildConfig
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.repository.CarDataRepository
import de.ixam97.carstatswidget.repository.CarDataStatus
import de.ixam97.carstatswidget.ui.MainViewModel
import de.ixam97.carstatswidget.ui.Screen
import de.ixam97.carstatswidget.ui.components.ErrorCard
import de.ixam97.carstatswidget.ui.components.LoggedInComponent
import de.ixam97.carstatswidget.ui.components.LoginRequiredCard
import de.ixam97.carstatswidget.ui.components.LogoutDialog
import de.ixam97.carstatswidget.util.AvailableApis

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(mainViewModel: MainViewModel, navController: NavController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val isKeyboardOpen by keyboardAsState()
    // val mainViewModel: MainViewModel = viewModel()
    val globalState by mainViewModel.globalState.collectAsState()
    val carInfoState by mainViewModel.carInfoState.collectAsState()
    val networkState by mainViewModel.networkState.collectAsState()
    val isRefreshing = (carInfoState.carDataInfo.status == CarDataStatus.Loading)
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { mainViewModel.requestCarData() })

    var showMenu by remember { mutableStateOf(false) }

    var logoutDialogApi by remember { mutableStateOf<AvailableApis?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        color = MaterialTheme.colorScheme.surface
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text ("Car Stats Widget") },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        // if (globalState.isLoggedIn == true) {
                        //     IconButton(
                        //         onClick = { mainViewModel.logoutPressed() }
                        //     ) {
                        //         Icon(
                        //             imageVector = Icons.Outlined.Logout,
                        //             contentDescription = null
                        //         )
                        //     }
                        // }

                        IconButton(
                            onClick = { navController.navigate(Screen.About.route) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null
                            )
                        }

                        // IconButton(
                        //     onClick = { mainViewModel.polestarTest() }
                        // ) {
                        //     Icon(
                        //         imageVector = Icons.Default.Construction,
                        //         contentDescription = null
                        //     )
                        // }

                        IconButton(
                            onClick = { showMenu = !showMenu },
                            enabled = networkState.connected?:false && globalState.isLoggedIn != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            for (api in AvailableApis.list) {
                                if (globalState.loggedInApis.contains(api)) {
                                    DropdownMenuItem(
                                        onClick = {
                                            logoutDialogApi = api
                                            showMenu = false
                                        },
                                    ) {
                                        Icon(Icons.Default.Delete, null)
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Text(CarDataRepository.selectApi(api).name)
                                    }
                                } else {
                                    DropdownMenuItem(
                                        onClick = { navController.navigate(Screen.Login.route + "/" + CarDataRepository.selectApi(api).name) },
                                    ) {
                                        Icon(Icons.Default.Add, null)
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Text(CarDataRepository.selectApi(api).name)
                                    }
                                }
                            }
                        }

                    }
                )
            },
            floatingActionButton = {
                when (globalState.loggedInApis.isNotEmpty()) {
                    true -> {
                        if (!isRefreshing) {
                            ExtendedFloatingActionButton(
                                modifier = Modifier
                                    .consumeWindowInsets(WindowInsets.navigationBars)
                                    .imePadding(),
                                text = { Text(stringResource(R.string.refresh_button_label)) },
                                icon = { Icon(Icons.Default.Refresh, null) },
                                onClick = { mainViewModel.requestCarData() },
                            )
                        }
                    }
                    /*
                    false -> {
                        ExtendedFloatingActionButton(
                            modifier = Modifier
                                .consumeWindowInsets(WindowInsets.navigationBars)
                                .imePadding(),
                            text = { Text(stringResource(R.string.login_button_label)) },
                            icon = { Icon(Icons.Default.Login, null)},
                            onClick = { mainViewModel.loginPressed() },
                        )
                    }
                    */
                    else  -> {}
                }
            }
        ) { innerPadding ->

            val modifiedPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                top = innerPadding.calculateTopPadding(),
                bottom = when (isKeyboardOpen) {
                    false -> innerPadding.calculateBottomPadding()
                    true -> WindowInsets.ime.asPaddingValues(LocalDensity.current).calculateBottomPadding()
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(modifiedPadding)
                    .consumeWindowInsets(modifiedPadding)
                    .pullRefresh(pullRefreshState, globalState.loggedInApis.isNotEmpty())

            ) {

                if (logoutDialogApi != null) {
                    LogoutDialog(
                        onDismissRequest = {
                            logoutDialogApi = null
                        },
                        onConfirmation = {
                            mainViewModel.logoutApi(logoutDialogApi!!)
                            mainViewModel.requestCarData()
                            logoutDialogApi = null
                        },
                        apiName = CarDataRepository.selectApi(logoutDialogApi!!).name
                    )
                }

                Column (modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                ) {
                    if (networkState.connected == false) {
                        ErrorCard(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            message = "Network connection Error: ${networkState.message}",
                            viewModel = mainViewModel)
                    }
                    when (globalState.isLoggedIn) {
                        // false -> {
                        //     TibberLogin(viewModel = mainViewModel)
                        // }
                        false -> {
                            if (networkState.connected == true) {
                                LoginRequiredCard(
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        true -> {
                            LoggedInComponent(mainViewModel)
                        }
                        else -> {
                            if (networkState.connected == true) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .height(56.dp)
                    )
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        modifier = Modifier
                            .alpha(0.38f)
                            .fillMaxWidth(),
                        text = "V ${BuildConfig.VERSION_NAME}",
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                    )
                }
                PullRefreshIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter),
                    refreshing = isRefreshing,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    state = pullRefreshState)
            }
        }
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(isImeVisible)
}