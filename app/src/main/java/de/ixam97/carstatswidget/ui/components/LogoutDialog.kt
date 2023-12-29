package de.ixam97.carstatswidget.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.util.AvailableApis

@Composable
fun LogoutDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    apiName: String
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Logout, contentDescription = null)
        },
        title = {
            Text(text = stringResource(R.string.dialog_logout_title))
        },
        text = {
            Text(text = stringResource(R.string.dialog_logout_message, apiName))
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}