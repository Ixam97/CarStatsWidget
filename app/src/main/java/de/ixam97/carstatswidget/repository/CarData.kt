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
    // val showLastSeen: Boolean = true,
    // val showVehicleName: Boolean = true,
    // val vehicleIds: List<String> = emptyList()
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