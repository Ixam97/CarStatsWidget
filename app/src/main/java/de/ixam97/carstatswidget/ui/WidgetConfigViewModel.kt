package de.ixam97.carstatswidget.ui

import android.app.Application
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ixam97.carstatswidget.WidgetData
import de.ixam97.carstatswidget.repository.CarDataInfoStateDefinition
import de.ixam97.carstatswidget.repository.CarDataRepository
import de.ixam97.carstatswidget.repository.CarDataStatus
import de.ixam97.carstatswidget.repository.CarDataWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WidgetConfigViewModel(application: Application) : AndroidViewModel(application = application) {

    companion object {
        private const val TAG = "WidgetConfigViewModel"
    }

    val manager = GlanceAppWidgetManager(application)

    private var widgetGlanceId: GlanceId? = null

    private val _widgetConfigState = MutableStateFlow(WidgetConfigState())
    val widgetConfigState = _widgetConfigState.asStateFlow()

    data class WidgetConfigState(
        val showVehicleNames: Boolean = true,
        val showLastSeenDates: Boolean = true,
        val vehicleList: List<Vehicle> = emptyList(),
        val done: Boolean = false,
        val ready: Boolean = false,
        val dataStatus: CarDataStatus = CarDataStatus.Loading
    ) {
        data class Vehicle(
            val id: String = "",
            val name: String = "",
            val isVisible: Boolean = false
        )
    }

    init {
        viewModelScope.launch {
            CarDataRepository.carDataInfoState.collect {carDataInfo ->
                if (!widgetConfigState.value.ready) {
                    val vehicleList: MutableList<WidgetConfigState.Vehicle> = mutableListOf()
                    if (carDataInfo.carData.isEmpty()) {
                        CarDataWorker.enqueue(application, true)
                    }
                    carDataInfo.carData.forEach {
                        vehicleList.add(WidgetConfigState.Vehicle(
                            id = it.id,
                            name = it.name
                        ))
                    }
                    _widgetConfigState.update {
                        it.copy(
                            vehicleList = vehicleList,
                            ready = false,
                            dataStatus = carDataInfo.status
                        )
                    }
                    widgetGlanceId?.apply {
                        loadWidgetState(this)
                    }
                }
            }
        }
    }

    fun changeShowVehicleNames() {
        _widgetConfigState.update {
            it.copy(showVehicleNames = !it.showVehicleNames)
        }
    }

    fun changeShowLastSeenDates() {
        _widgetConfigState.update {
            it.copy(showLastSeenDates = !it.showLastSeenDates)
        }
    }

    fun changeSelectedVehicleId(id: String) {
        _widgetConfigState.update { widgetConfigState ->
            val newVehicleList = widgetConfigState.vehicleList.toMutableList()
            val vehicle = newVehicleList.find { it.id == id }
            vehicle?.apply {
                newVehicleList[newVehicleList.indexOf(vehicle)] = vehicle.copy(isVisible = !vehicle.isVisible)
            }
            widgetConfigState.copy(vehicleList = newVehicleList)
        }
    }
    fun clickedDone() {
        viewModelScope.launch {
            widgetGlanceId?.apply {
                WidgetData.updateConfig(
                    context = getApplication(),
                    config = WidgetData.WidgetConfig(
                        showVehicleName = widgetConfigState.value.showVehicleNames,
                        showLastSeen = widgetConfigState.value.showLastSeenDates,
                        vehicleIds = widgetConfigState.value.vehicleList.filter { it.isVisible }.map { it.id }
                    ),
                    glanceId = this
                )
            }
            _widgetConfigState.update {
                CarDataWorker.enqueue(getApplication(), true)
                it.copy(done = true)
            }
        }
    }

    fun setGlanceId(glanceId: GlanceId) {
        widgetGlanceId = glanceId
        widgetGlanceId?.apply {
            loadWidgetState(this)
        }
    }

    private fun loadWidgetState(glanceId: GlanceId) {
        Log.d(TAG, "Loading state of Widget with Glance ID ${glanceId.toString()}")
        viewModelScope.launch {
            val currentWidgetState = getAppWidgetState(
                context = getApplication(),
                definition = CarDataInfoStateDefinition,
                glanceId = glanceId
            )
            _widgetConfigState.update {
                val vehicles = it.vehicleList.toMutableList()

                it.vehicleList.forEachIndexed { index, vehicle ->
                    vehicles[index] = vehicle.copy(isVisible = currentWidgetState.vehicleIds.contains(vehicle.id))
                }

                it.copy(
                    showLastSeenDates = currentWidgetState.showLastSeen,
                    showVehicleNames = currentWidgetState.showVehicleName,
                    vehicleList = vehicles,
                    ready = it.vehicleList.isNotEmpty() && widgetGlanceId != null
                )
            }
        }
    }
}