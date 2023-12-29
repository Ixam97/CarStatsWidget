package de.ixam97.carstatswidget.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.ui.LoginViewModel
import de.ixam97.carstatswidget.ui.MainViewModel
import de.ixam97.carstatswidget.util.AvailableApis

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
/**
 * Composable to display a login form
 */
@Composable
fun LoginScreen(mainViewModel: MainViewModel, navController: NavController, selectedApi: AvailableApis) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val isKeyboardOpen by keyboardAsState()
    val viewModel: LoginViewModel = viewModel()
    viewModel.selectedApi = selectedApi

    val loginState by viewModel.loginState.collectAsState()
    val mail = viewModel.mail
    val password = viewModel.pass

    LaunchedEffect(Unit) {
        viewModel.popBackstack.collect {
            if (it) {
                mainViewModel.validateLogins()
                mainViewModel.requestCarData()
                navController.popBackStack()
            }
        }
    }

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
                    title = {
                        when (selectedApi) {
                            AvailableApis.Polestar -> {
                                Text("Polestar Login")
                            }
                            AvailableApis.Tibber -> {
                                Text("Tibber Login")
                            }
                        }
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
            },
            floatingActionButton = {
                if (loginState.loginPossible) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .consumeWindowInsets(WindowInsets.navigationBars)
                            .imePadding(),
                        text = { Text(stringResource(R.string.login_button_label)) },
                        icon = { Icon(Icons.Default.Login, null) },
                        onClick = { viewModel.loginPressed() },
                    )
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

            ) {
                Column (modifier = Modifier
                       // .padding(16.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = mail,
                            singleLine = true,
                            label = { Text(text = stringResource(R.string.label_mail)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            onValueChange = { viewModel.mailEntered(it) }
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = password,
                            singleLine = true,
                            visualTransformation = if (loginState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            label = { Text(text = stringResource(R.string.label_password)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.passwordHideToggle() }
                                ) {
                                    when (loginState.passwordVisible) {
                                        true -> {
                                            Icon(
                                                Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }

                                        else -> {
                                            Icon(
                                                Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            },
                            onValueChange = { viewModel.passwordEntered(it) }
                        )

                        if (loginState.loginFailed) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Outlined.Error,
                                        contentDescription = null
                                    )
                                    Text(
                                        text = stringResource(R.string.login_failed_prompt)
                                    )
                                }
                            }
                        }
                        AvailableApis.loginHints(selectedApi)?.let {hintResource ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Outlined.WarningAmber,
                                        contentDescription = "Info icon"
                                    )
                                    Text(
                                        text = stringResource(hintResource)
                                    )
                                }
                            }
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) //.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = "Info icon"
                                )
                                Text(
                                    text = stringResource(R.string.login_data_hint)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}