package de.ixam97.carstatswidget.repository

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

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
    val showVehicleName: Boolean = true,
    val vehicleIds: List<String> = emptyList()
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