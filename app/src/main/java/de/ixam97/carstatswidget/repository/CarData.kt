package de.ixam97.carstatswidget.repository

import android.content.Context
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
sealed interface CarDataInfo {

    @Serializable
    object Loading : CarDataInfo

    @Serializable
    object NotLoggedIn: CarDataInfo

    @Serializable
    data class Available(
        val carData: List<CarData>,
        val showLastSeen: Boolean = true,
        val showVehicleName: Boolean = true
    ) : CarDataInfo

    @Serializable
    data class Unavailable(val message: String) : CarDataInfo

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
        override val defaultValue = CarDataInfo.Unavailable("no data")

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

class CarDataWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private val uniqueWorkName = CarDataWorker::class.java.simpleName

        fun enqueue(context: Context, force: Boolean = false) {
            val manager = WorkManager.getInstance(context)
            val requestBuilder = PeriodicWorkRequestBuilder<CarDataWorker>(
                15,
                TimeUnit.MINUTES
            )
            var workPolicy = ExistingPeriodicWorkPolicy.KEEP
            if (force) {
                workPolicy = ExistingPeriodicWorkPolicy.UPDATE
            }

            manager.enqueueUniquePeriodicWork(
                uniqueWorkName,
                workPolicy,
                requestBuilder.build()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
        }
    }

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(StateOfChargeWidget::class.java)
        return try {
            val preferencesManager = de.ixam97.carstatswidget.PreferencesManager(context = context)
            val email = preferencesManager.getString("tibberMail", "")
            val password = preferencesManager.getString("tibberPassword", "")
            if (email == "" || password == "") {
                setWidgetState(glanceIds, CarDataInfo.NotLoggedIn)
            } else {
                val carDataInfo = CarDataRepository.getCarDataInfo(email, password)
                val carDataInfoWithSettings = if (carDataInfo is CarDataInfo.Available) {
                    carDataInfo.copy(
                        showLastSeen = preferencesManager.getBoolean("showLastSeen", true),
                        showVehicleName = preferencesManager.getBoolean("showVehicleName", true)
                    )
                } else {
                    carDataInfo
                }
                setWidgetState(glanceIds, carDataInfoWithSettings)
                Log.i("CarData", carDataInfoWithSettings.toString())
            }
            Result.success()
        } catch (e: Exception) {
            setWidgetState(glanceIds, CarDataInfo.Unavailable("Failed to load data"))
            Log.e("CarData", e.stackTraceToString())
            return Result.failure()
        }
    }

    private suspend fun setWidgetState(glanceIds: List<GlanceId>, newState: CarDataInfo) {
        glanceIds.forEach {glanceId ->
            updateAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId,
                updateState = { newState }
            )
            StateOfChargeWidget().update(context, glanceId)
        }
    }
}