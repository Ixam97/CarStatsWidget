package de.ixam97.carstatswidget.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.GlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.ixam97.carstatswidget.StateOfChargeWidget
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.Serializable
import java.lang.Exception
import java.util.concurrent.TimeUnit
@Serializable
sealed interface CarDataStatus {
    @Serializable
    object Available: CarDataStatus
    @Serializable
    object NotLoggedIn: CarDataStatus
    @Serializable
    object Unavailable: CarDataStatus
    @Serializable
    object Loading: CarDataStatus
}

@Serializable
data class CarDataInfo(
    val status: CarDataStatus,
    val message: String? = null,
    val carData: List<CarData> = emptyList(),
    val showLastSeen: Boolean = true,
    val showVehicleName: Boolean = true
) {
    @Serializable
    data class CarData(
        val stateOfCharge: Int,
        val lastSeen: String,
        val lastUpdated: String,
        val imgUrl: String,
        val name: String,
        val shortName: String,
        val id: String
    )
}

object CarDataInfoStateDefinition: GlanceStateDefinition<CarDataInfo> {

    private const val DATA_STORE_FILENAME = "carDataInfo"

    private val Context.datastore by dataStore(DATA_STORE_FILENAME, CarDataInfoSerializer)

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<CarDataInfo> {
        return context.datastore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    object CarDataInfoSerializer : Serializer<CarDataInfo> {
        override val defaultValue = CarDataInfo(CarDataStatus.Unavailable, message = "No data")

        override suspend fun readFrom(input: InputStream): CarDataInfo = try {
            Json.decodeFromString(
                CarDataInfo.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Could not read data: ${exception.message}")
        }

        override suspend fun writeTo(t: CarDataInfo, output: OutputStream) {
            output.use {
                it.write(
                    Json.encodeToString(CarDataInfo.serializer(), t).encodeToByteArray()
                )
            }
        }
    }
}