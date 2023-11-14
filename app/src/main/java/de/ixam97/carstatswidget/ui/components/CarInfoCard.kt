package de.ixam97.carstatswidget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import de.ixam97.carstatswidget.R
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.ui.MainViewModel

@Composable
fun CarInfoCard(viewModel: MainViewModel) {
    val carInfoState by viewModel.carInfoState.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        when (carInfoState.carDataInfo) {
            is CarDataInfo.Available -> {
                for (carData in (carInfoState.carDataInfo as CarDataInfo.Available).carData) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                    ) {
                        CarInfo(
                            carData = carData,
                            refresh = { viewModel.requestCarData() }
                        )
                    }
                }
            }
            is CarDataInfo.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
            is CarDataInfo.Unavailable -> {
                val unavailableCarInfoState = carInfoState.carDataInfo as CarDataInfo.Unavailable
                ErrorCard(unavailableCarInfoState.message, viewModel)
                if (unavailableCarInfoState.carData != null) {
                    for (carData in unavailableCarInfoState.carData) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                            )
                        ) {
                            CarInfo(
                                carData = carData,
                                refresh = { viewModel.requestCarData() }
                            )
                        }
                    }
                }
            }
            is CarDataInfo.NotLoggedIn -> {
                ErrorCard((carInfoState.carDataInfo as CarDataInfo.NotLoggedIn). message, viewModel)
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, viewModel: MainViewModel) {

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Default.Error, null)
                Text(
                    fontWeight = FontWeight.Bold,
                    text = stringResource(R.string.data_unavailable_prompt)
                )
            }
            Text(
                // modifier = Modifier.weight(1f),
                text = message
            )
            Button(
                onClick = { viewModel.requestCarData() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(R.string.retry_button_label))
            }
        }
    }
}

@Composable
fun CarInfo(carData: CarDataInfo.CarData, refresh: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = carData.name,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = carData.id,
            fontSize = 9.sp,
            color = Color(1f, 1f, 1f, 0.4f)
        )
        SubcomposeAsyncImage(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(200.dp),
            model = carData.imgUrl,
            contentDescription = "Tibber Image",
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                        .width(40.dp)
                )
            },
            error = {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = "Failed to load image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        )
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Surface(
                modifier =  Modifier
                    .weight(1f)
                    .width(100.dp)
                    .height(40.dp),
                shape = MaterialTheme.shapes.medium,
                // border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                // color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
            ) {
                Box (
                    modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                colorResource(R.color.club_violet),
                                colorResource(R.color.club_blue)
                            )
                        )
                    ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = if (!isSystemInDarkTheme()) {
                                Color(1f, 1f, 1f, 0.3f)
                            } else {
                                Color.Transparent
                            }
                        ),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth( 1 - carData.stateOfCharge.toFloat() / 100f)
                            .background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = if (isSystemInDarkTheme()) {
                                        Color(1f,1f,1f,0.05f)
                                    } else {
                                        Color(0f,0f,0f,0.1f)
                                    }
                                )
                        )
                    }
                }
            }
            Text(
                text = "${carData.stateOfCharge}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = stringResource(R.string.last_seen_label, carData.lastSeen))
        Text(text = stringResource(R.string.last_request_label, carData.lastUpdated))
        Spacer(modifier = Modifier.size(8.dp))
    }
}