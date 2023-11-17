package de.ixam97.carstatswidget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.Dimension
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.CarDataInfoStateDefinition
import de.ixam97.carstatswidget.repository.CarDataStatus
import de.ixam97.carstatswidget.repository.CarDataWorker
import de.ixam97.carstatswidget.ui.MainActivity
import de.ixam97.carstatswidget.util.ResizeBitmap
import de.ixam97.carstatswidget.util.getAspectRatio
import de.ixam97.carstatswidget.util.getResizedBitmap
import kotlinx.coroutines.delay

class StateOfChargeWidget : GlanceAppWidget() {

    val TAG = "Widget"

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
                    when (carDataInfo.status) {
                        is CarDataStatus.NotLoggedIn -> {
                            NotLoggedInComponent()
                        }
                        is CarDataStatus.Loading,
                        is CarDataStatus.Unavailable,
                        is CarDataStatus.Available -> {
                            Column (
                                modifier = GlanceModifier
                                    .fillMaxHeight()
                            ) {
                                val filteredCarData = carDataInfo.carData.filter{
                                    carDataInfo.vehicleIds.contains(it.id)
                                }
                                val size = LocalSize.current
                                val gap = 2.dp
                                val barHeight = (size.height - (20.dp + ((gap.times(filteredCarData.size - 1))))) / filteredCarData.size
                                filteredCarData.forEachIndexed {index, carData ->
                                    Box (modifier = GlanceModifier.defaultWeight()) {
                                        AvailableComponent(
                                            carDataInfo = carDataInfo,
                                            carData = carData,
                                            availableHeight = barHeight)
                                    }
                                    if (index < filteredCarData.size - 1) {
                                        Spacer(modifier = GlanceModifier.size(gap))
                                    }
                                }
                                if (filteredCarData.isEmpty()) {
                                    UnavailableComponent("No data available")
                                }
                            }
                            when (carDataInfo.status) {
                                CarDataStatus.Unavailable -> {
                                    Image(
                                        modifier = GlanceModifier.padding(10.dp),
                                        provider = ImageProvider(R.drawable.ic_offline),
                                        contentDescription =  null,
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.Red))
                                    )
                                }
                                CarDataStatus.Loading -> {
                                    // Image(
                                    //     modifier = GlanceModifier.padding(10.dp),
                                    //     provider = ImageProvider(R.drawable.ic_hourglas),
                                    //     contentDescription =  null,
                                    //     colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                                    // )
                                    CircularProgressIndicator(
                                        modifier = GlanceModifier
                                            .padding(4.dp)
                                            .size(30.dp),
                                        color = GlanceTheme.colors.onSurfaceVariant
                                    )
                                }
                                else -> {}
                            }
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

    /*
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
*/
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
        val mainActivityIntent = Intent(LocalContext.current, MainActivity::class.java)
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        Column(
            modifier = GlanceModifier
                .background(GlanceTheme.colors.errorContainer)
                .clickable(onClick = actionStartActivity(mainActivityIntent))// actionRunCallback<UpdateCarDataAction>())
                .cornerRadius(15.dp)
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                style = TextStyle(color = GlanceTheme.colors.onErrorContainer),
                text = message
            )
        }
    }

    @Composable
    private fun AvailableComponent(
        modifier: GlanceModifier = GlanceModifier,
        carDataInfo: CarDataInfo,
        carData: CarDataInfo.CarData,
        availableHeight: Dp
    ) {
        val mainActivityIntent = Intent(LocalContext.current, MainActivity::class.java)
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val url = carData.imgUrl
        var carImageBitmap by remember(carData.id) { mutableStateOf<Bitmap?>(null) }
        val size = LocalSize.current
        val context = LocalContext.current

        val showImage = size.width > 230.dp
        val showDate = carDataInfo.showLastSeen

        LaunchedEffect(carData.id) {
            Log.i("Widget", "Start image loading")
            val bitmapWasNull = carImageBitmap == null
            val tmpBitmap = context.getTibberImage(url)
            delay(100)
            carImageBitmap = tmpBitmap
            Log.i("Widget", "Image loading complete")
            if (bitmapWasNull && carImageBitmap != null) {
                Log.i("Widget", "Image now available, refreshing")
                CarDataWorker.enqueue(context = context, force = false)
            }
        }

        Box (
            modifier = modifier
                .fillMaxWidth()
                // .height(size.height / 2)
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

                    val carIcon = AppCompatResources.getDrawable(context, R.drawable.ic_car)
                    var imageProvider = ImageProvider(R.drawable.ic_car)
                    carIcon?.let {
                        DrawableCompat.setTint(
                            it,
                            GlanceTheme.colors.onSurfaceVariant.getColor(context).toArgb()
                        )
                        imageProvider = ImageProvider(it.toBitmap())
                    }
                    var imageHeight = 40.dp
                    var imageWidth = 40.dp

                    carImageBitmap?.let {
                        val resizedBitmap =
                            getResizedBitmap(it, 400, ResizeBitmap.Width)
                        imageHeight = availableHeight.coerceAtMost(70.dp)
                        imageWidth = imageHeight * getAspectRatio(resizedBitmap)
                        imageProvider = ImageProvider(resizedBitmap)
                    }
                    Image(
                        modifier = GlanceModifier
                            .height(imageHeight)
                            .width(imageWidth),
                        provider = imageProvider,
                        // contentScale = ContentScale.Fit,
                        contentDescription = "Tibber Image",
                    )


                    /*
                    carImageBitmap?.let {
                        val resizedBitmap =
                            getResizedBitmap(it, 400, ResizeBitmap.Width)
                        val imageHeight = (LocalSize.current.height).coerceAtMost(70.dp)
                        val imageWidth = imageHeight * getAspectRatio(resizedBitmap)
                        val imageProvider = ImageProvider(resizedBitmap)
                        Image(
                            modifier = GlanceModifier
                                .height(imageHeight)
                                .width(imageWidth),
                            provider = imageProvider,
                            // contentScale = ContentScale.Fit,
                            contentDescription = "Tibber Image",
                        )
                    }

                     */
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

object WidgetData {

    private const val TAG ="WidgetData"

    data class WidgetConfig(
        val showVehicleName: Boolean = true,
        val showLastSeen: Boolean = true,
        val vehicleIds: List<String> = emptyList()
    )

    suspend fun updateConfig(context: Context, config: WidgetConfig) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(StateOfChargeWidget::class.java)

        glanceIds.forEach {glanceId ->
            updateConfig(context, config, glanceId)
        }
    }

    suspend fun updateConfig(context: Context, config: WidgetConfig, glanceId: GlanceId) {
        val prevWidgetState = getAppWidgetState(
            context = context,
            definition = CarDataInfoStateDefinition,
            glanceId = glanceId)

        updateAppWidgetState(
            context = context,
            definition = CarDataInfoStateDefinition,
            glanceId = glanceId,
            updateState = {
                prevWidgetState.copy(
                    showVehicleName = config.showVehicleName,
                    showLastSeen = config.showLastSeen,
                    vehicleIds = config.vehicleIds
                )
            }
        )
        Log.d(TAG, "Updating config for Glance ID ${glanceId.toString()}")
        StateOfChargeWidget().update(context, glanceId)
    }
    suspend fun updateStatus(context: Context, status: CarDataStatus) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(StateOfChargeWidget::class.java)

        glanceIds.forEach {glanceId ->
            val prevWidgetState = getAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId)

            updateAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId,
                updateState = {
                    prevWidgetState.copy(
                        status = status
                    )
                }
            )
            StateOfChargeWidget().update(context, glanceId)
        }
    }
    suspend fun updateData(context: Context, data: List<CarDataInfo.CarData>) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(StateOfChargeWidget::class.java)

        glanceIds.forEach {glanceId ->
            val prevWidgetState = getAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId)

            updateAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId,
                updateState = {
                    prevWidgetState.copy(carData = data)
                }
            )
            StateOfChargeWidget().update(context, glanceId)
        }

    }
}