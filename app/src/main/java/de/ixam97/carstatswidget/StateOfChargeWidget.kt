package de.ixam97.carstatswidget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.CarDataInfoStateDefinition
import de.ixam97.carstatswidget.repository.CarDataWorker
import de.ixam97.carstatswidget.ui.MainActivity
import de.ixam97.carstatswidget.util.ResizeBitmap
import de.ixam97.carstatswidget.util.getAspectRatio
import de.ixam97.carstatswidget.util.getResizedBitmap
import kotlinx.coroutines.delay

class StateOfChargeWidget : GlanceAppWidget() {

    override val stateDefinition = CarDataInfoStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val fullSize = LocalSize.current
                Log.i("WidgetSize:", "width: ${fullSize.width}, height: ${fullSize.height}")
                Box(
                    modifier = GlanceModifier
                        .background(colorProvider = GlanceTheme.colors.onSecondary)
                        .fillMaxSize()
                        .cornerRadius(25.dp)
                        .padding(all = if (fullSize.height > 70.dp) 10.dp else 0.dp)
                    ) {
                    val carDataInfo = currentState<CarDataInfo>()
                    when (carDataInfo) {
                        is CarDataInfo.Loading -> {
                            LoadingComponent()
                        }
                        is CarDataInfo.NotLoggedIn -> {
                            NotLoggedInComponent()
                        }
                        is CarDataInfo.Available -> {
                            AvailableComponent(carDataInfo)
                        }
                        is CarDataInfo.Unavailable -> {
                            UnavailableComponent(carDataInfo.message)
                        }
                    }
                }
                /*
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.primaryContainer),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text(
                        text = stateOfCharge.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 26.sp
                        )
                    )
                    Text(
                        text = currentTime.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontWeight = FontWeight.Medium,
                            fontSize = 26.sp
                        )
                    )
                    Button(
                        text = "Update",
                        onClick = actionSendBroadcast(WidgetUpdateReceiver::class.java)
                    )
                }

                 */
            }
        }
    }

    @Composable
    private fun LoadingComponent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                text = "Loading..."
            )
        }
    }

    @Composable
    private fun NotLoggedInComponent() {
        val mainActivityIntent = Intent(LocalContext.current, MainActivity::class.java)
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(onClick = actionStartActivity(mainActivityIntent)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                text = "Not logged in"
            )
        }
    }

    @Composable
    private fun UnavailableComponent(message: String = "Unavailable") {
        Column(
            modifier = GlanceModifier
                .clickable(onClick = actionRunCallback<UpdateCarDataAction>())
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                text = message
            )
            Button(
                text = "Reload",
                onClick = actionRunCallback<UpdateCarDataAction>()
            )
        }
    }

    @Composable
    private fun AvailableComponent(carDataInfo: CarDataInfo.Available) {
        val carData = carDataInfo.carData[0]
        val mainActivityIntent = Intent(LocalContext.current, MainActivity::class.java)
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val url = carData.imgUrl
        var carImageBitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
        val size = LocalSize.current
        val context = LocalContext.current

        val showImage = size.width > 230.dp
        val showDate = /* (size.height > 110.dp || showImage) &&*/ carDataInfo.showLastSeen

        LaunchedEffect(url) {
            carImageBitmap = context.getTibberImage(url)
            Log.i("Widget", "LaunchedEffect")
        }

        Box (
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(onClick = actionRunCallback<UpdateCarDataAction>())
        ) {
            Box (
                modifier = GlanceModifier
                    // .background(GlanceTheme.colors.primaryContainer)
                    .background(ImageProvider(R.drawable.bg_club))
                    .fillMaxSize()
                    .cornerRadius(15.dp)
            ) {
                Box (

                    modifier = GlanceModifier
                        .background(
                            day = Color(1f,1f,1f,0.3f),
                            night = Color.Transparent
                        )
                        .fillMaxSize()


                ) { }
                Row (
                    horizontalAlignment = Alignment.Horizontal.End,
                    modifier = GlanceModifier.fillMaxSize()
                ) {

                    Box (
                        modifier = GlanceModifier
                            .background(GlanceTheme.colors.onSecondary)
                            .width((size.width - 20.dp) * (1 - (carData.stateOfCharge.toFloat() / 100f)))
                            .fillMaxHeight()
                    ) {
                        Box (
                            modifier = GlanceModifier
                                .background(
                                    day = Color(0f,0f,0f,0.07f),
                                    night = Color(0f,0f,0f,0.3f))
                                .fillMaxSize()
                        ) { }
                    }


                }
            }
            Row (
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                if (showImage) {
                    if (carImageBitmap != null) {
                        val resizedBitmap = getResizedBitmap(carImageBitmap!!, 400, ResizeBitmap.Width)
                        val imageHeight = (LocalSize.current.height).coerceAtMost(70.dp)
                        val imageWidth = imageHeight * getAspectRatio(resizedBitmap)
                        Image(
                            modifier = GlanceModifier
                                .height(imageHeight)
                                .width(imageWidth),
                            provider = ImageProvider(resizedBitmap),
                            // contentScale = ContentScale.Fit,
                            contentDescription = "Tibber Image"
                        )
                    } else {
                        Image(
                            provider = ImageProvider(R.drawable.ic_car),
                            contentDescription = "Placeholder",
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                        )
                        LaunchedEffect(null) {
                            delay(200)
                            StateOfChargeWidget().updateAll(context)
                        }
                    }
                }

/*
                Image(
                    provider = ImageProvider(R.drawable.ic_bolt),
                    contentDescription = "charging",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                )

 */

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clickable(actionStartActivity(mainActivityIntent))
                ) {
                    if (carDataInfo.showVehicleName) {
                        Text(
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                fontSize = 18.sp
                            ),
                            text = carData.shortName
                        )
                    }
                    Text(
                        modifier = GlanceModifier.padding(vertical = (-5).dp),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End
                        ),
                        text = "${carData.stateOfCharge}%"
                    )
                    if (showDate) {
                        Text(
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End,
                                fontSize = 10.sp
                            ),
                            text = carData.lastSeen
                        )
                    }
                }
            }
        }
    }

    private suspend fun Context.getTibberImage(url: String, force: Boolean = false): Bitmap? {
        val request = ImageRequest.Builder(this).data(url).apply {
            if (force) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
            }
        }.build()

        return when (val result = imageLoader.execute(request)) {
            is ErrorResult -> {
                null
            }
            is SuccessResult -> {
                result.drawable.toBitmapOrNull()
            }
        }
    }
}

class StateOfChargeWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StateOfChargeWidget()
}

class UpdateCarDataAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        CarDataWorker.enqueue(context = context, force = true)
    }
}