package de.ixam97.carstatswidget.widgets

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.CarDataStatus

object StateOfChargeWidgetData {

    private const val TAG ="WidgetData"

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
            definition = StateOfChargeWidgetStateDefinition,
            glanceId = glanceId)

        updateAppWidgetState(
            context = context,
            definition = StateOfChargeWidgetStateDefinition,
            glanceId = glanceId,
            updateState = {
                prevWidgetState.copy(
                    widgetConfig = config
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
                definition = StateOfChargeWidgetStateDefinition,
                glanceId = glanceId)

            updateAppWidgetState(
                context = context,
                definition = StateOfChargeWidgetStateDefinition,
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
                definition = StateOfChargeWidgetStateDefinition,
                glanceId = glanceId)

            updateAppWidgetState(
                context = context,
                definition = StateOfChargeWidgetStateDefinition,
                glanceId = glanceId,
                updateState = {
                    prevWidgetState.copy(carData = data)
                }
            )
            StateOfChargeWidget().update(context, glanceId)
        }

    }
}