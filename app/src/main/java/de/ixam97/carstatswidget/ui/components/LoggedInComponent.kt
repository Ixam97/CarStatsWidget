package de.ixam97.carstatswidget.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.ui.MainViewModel

@Composable
fun LoggedInComponent(viewModel: MainViewModel) {
    val globalState by viewModel.globalState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Card( modifier = Modifier
            .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.login_name, viewModel.tibberMail) //"Logged in as ${viewModel.tibberMail}"
            )
        }
        CarInfoCard(viewModel)

        Button(
            onClick = {viewModel.requestCarData()},
        ) {
            Text(text = stringResource(R.string.refresh_button_label))
        }
        Column {
            LabelledCheckbox(
                modifier = Modifier.fillMaxWidth(),
                checked = globalState.showLastSeen?:false,
                enabled = globalState.showLastSeen != null,
                onCheckedChange = { checked ->
                    viewModel.setShowLastSeen(checked)},
                label = stringResource(R.string.checkbox_widget_seen_date)
            )
            LabelledCheckbox(
                modifier = Modifier.fillMaxWidth(),
                checked = globalState.showVehicleName?:false,
                enabled = globalState.showVehicleName != null,
                onCheckedChange =  { checked ->
                    viewModel.setShowVehicleName(checked)},
                label = stringResource(R.string.checkbox_widget_vehicle_name)
            )
        }
    }
}