package de.ixam97.carstatswidget.repository

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.ixam97.carstatswidget.StateOfChargeWidget
import java.lang.Exception
import java.util.concurrent.TimeUnit

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
                setWidgetState(glanceIds, CarDataInfo(status = CarDataStatus.NotLoggedIn))
            } else {
                val carDataInfo = CarDataRepository.getCarDataInfo(email, password)
                val carDataInfoWithSettings = carDataInfo.copy(
                    showLastSeen = preferencesManager.getBoolean("showLastSeen", true),
                    showVehicleName = preferencesManager.getBoolean("showVehicleName", true)
                )
                setWidgetState(glanceIds, carDataInfoWithSettings)
                Log.i("CarData", carDataInfoWithSettings.toString())
            }
            Result.success()
        } catch (e: Exception) {
            setWidgetState(glanceIds, CarDataInfo(
                status = CarDataStatus.Unavailable,
                message = "Loading data failed: ${e.localizedMessage?: "Unknown error"}"
            ))
            Log.e("CarData", e.stackTraceToString())
            return Result.failure()
        }
    }

    private suspend fun setWidgetState(glanceIds: List<GlanceId>, newState: CarDataInfo) {
        glanceIds.forEach {glanceId ->


            val prevData = getAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId)

            Log.d("CarDataWorker", "Car data length in widget state: ${prevData.carData.size}")

            updateAppWidgetState(
                context = context,
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId,
                updateState = { newState.copy(carData = prevData.carData) }
            )
            StateOfChargeWidget().update(context, glanceId)
        }
    }
}