package de.ixam97.carstatswidget.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.ui.MainViewModel

// @Composable
/*
fun TibberLogin(modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel()) {
    val tibberLoginState by viewModel.tibberLoginState.collectAsState()
    val mail = viewModel.tibberMail
    val password = viewModel.tibberPassword
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.login_prompt)
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = mail,
            singleLine = true,
            label = { Text(text = stringResource(R.string.label_mail)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            onValueChange = { /* viewModel.mailEntered(it) */ }
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = password,
            singleLine = true,
            visualTransformation = if (tibberLoginState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            label = { Text(text = stringResource(R.string.label_password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(
                    onClick = { /* viewModel.passwordHideToggle() */ }
                ) {
                    when (tibberLoginState.passwordVisible) {
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
            onValueChange = { /* viewModel.passwordEntered(it) */ }
        )
        if (tibberLoginState.loginFailed) {
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
        // Button(
        //     modifier = Modifier.fillMaxWidth(),
        //     onClick = { viewModel.loginPressed() },
        //     enabled = tibberLoginState.loginPossible
        // ) {
        //     Text(text = stringResource(R.string.login_button_label))
        // }
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
*/