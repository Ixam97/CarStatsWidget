package de.ixam97.carstatswidget.repository

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
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
                setWidgetState(glanceIds, CarDataInfo.NotLoggedIn())
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