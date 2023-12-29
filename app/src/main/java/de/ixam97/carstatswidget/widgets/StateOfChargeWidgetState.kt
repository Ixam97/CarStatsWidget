package de.ixam97.carstatswidget.widgets

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.CarDataStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class StateOfChargeWidgetState(
    val status: CarDataStatus,
    val message: String? = null,
    val carData: List<CarDataInfo.CarData> = emptyList(),
    val widgetConfig: WidgetConfig = WidgetConfig()
)

@Serializable
data class WidgetConfig(
    val showVehicleName: Boolean = true,
    val showLastSeen: Boolean = true,
    val vehicleIds: List<String> = emptyList(),
    val basicLayout: Boolean = false
)

object StateOfChargeWidgetStateDefinition: GlanceStateDefinition<StateOfChargeWidgetState> {

    private const val DATA_STORE_FILENAME_PREFIX = "carDataInfo_"

    // private val Context.datastore by dataStore(DATA_STORE_FILENAME, CarDataInfoSerializer)

    // override suspend fun getDataStore(context: Context, fileKey: String): DataStore<CarDataInfo> {
    //     return context.datastore
    // }

    override suspend fun getDataStore(context: Context, fileKey: String) = DataStoreFactory.create(
        serializer = CarDataInfoSerializer,
        produceFile = { getLocation(context, fileKey)}
    )

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME_PREFIX + fileKey)
    }

    object CarDataInfoSerializer : Serializer<StateOfChargeWidgetState> {
        override val defaultValue = StateOfChargeWidgetState(CarDataStatus.Unavailable, message = "No data")

        override suspend fun readFrom(input: InputStream): StateOfChargeWidgetState = try {
            Json.decodeFromString(
                StateOfChargeWidgetState.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            Log.e("Widget Data Store", "Could not read data: ${exception.message}")
            StateOfChargeWidgetState(CarDataStatus.ConfigChanged)
        }

        override suspend fun writeTo(t: StateOfChargeWidgetState, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(StateOfChargeWidgetState.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}
