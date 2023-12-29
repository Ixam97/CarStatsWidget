package de.ixam97.carstatswidget.repository

import android.content.Context
import android.util.Log
import de.ixam97.carstatswidget.util.AvailableApis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CarDataRepository {
    private val TAG = "CarDataRepository"

    private val _carDataInfoState = MutableStateFlow<CarDataInfo>(CarDataInfo(CarDataStatus.Unavailable))
    val carDataInfoState: StateFlow<CarDataInfo> = _carDataInfoState.asStateFlow()

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState())
    val networkState = _networkState.asStateFlow()

    data class NetworkState(
        val connected: Boolean? = null,
        val message: String = ""
    )

    //var status: CarDataStatus = CarDataStatus.Available
    //    private set
    //var message = ""
    //    private set

    fun setNetworkError(message: String = "Unknown Error") {
        // _carDataInfoState.update {
        //     it.copy(
        //         status = CarDataStatus.Unavailable,
        //         message = "Network connection error"
        //     )
        // }
        _networkState.update {
            it.copy(connected = false, message = message)
        }
    }

    fun resetNetworkError() {
        _networkState.update {
            it.copy(connected = true, message = "")
        }
    }

    fun startNetworkAttempt() {
        if (networkState.value.connected != true) {
            _networkState.update {
                it.copy(connected = null)
            }
        }
    }

    suspend fun getLoggedInApis(context: Context): List<AvailableApis> {
        // var preferencesManager = PreferencesManager(context = context)
        val apisList = mutableListOf<AvailableApis>()

        for (api in AvailableApis.list) {
            if (verifyLogin(api, context)) {
                apisList.add(api)
            }
        }
/*
        if (!PolestarRepository.isLoggedIn()) {
            val mail = preferencesManager.getString("polestarMail", "")
            val pass = preferencesManager.getString("polestarPassword", "")
            Log.i(TAG, "Polestar: $mail, $pass")
            if (mail != "" && pass != "") {
                 if (PolestarRepository.login(ApiCredentials(mail, pass)) != null) {
                     apisList.add(AvailableApis.Polestar)
                 }
            }
        } else {
            apisList.add(AvailableApis.Polestar)
        }

        if (!TibberRepository.isLoggedIn()) {
            val mail = preferencesManager.getString("tibberMail", "")
            val pass = preferencesManager.getString("tibberPassword", "")
            Log.i(TAG, "Tibber: $mail, $pass")
            if (mail != "" && pass != "") {
                if (TibberRepository.verifyLoginData(mail, pass)) {
                    apisList.add(AvailableApis.Tibber)
                }
            }
        } else {
            apisList.add(AvailableApis.Tibber)
        }
*/
        return apisList
    }

    suspend fun logout(api: AvailableApis, context: Context) {
        val repository = selectApi(api)
        repository.logout(context)
        Log.i(TAG, "${repository.name} logut.")
    }

    suspend fun login(api: AvailableApis, credentials: ApiCredentials, context: Context): Boolean {
        val repository = selectApi(api)
        val result = repository.login(context = context, apiCredentials = credentials)
        Log.i(TAG, "${repository.name} login: $result.")
        return result
    }

    suspend fun verifyLogin(api: AvailableApis, context: Context): Boolean {
        val repository = selectApi(api)
        val result =  repository.verifyLogin(context)
        Log.i(TAG, "${repository.name} login verified: $result.")
        return result
    }

    suspend fun getVehicles(api: AvailableApis, context: Context): List<CarDataInfo.CarData>? {
        val repository = selectApi(api)
        return repository.getVehicles(context)
    }

    suspend fun getVehicles(context: Context): List<CarDataInfo.CarData> {
        // var dataUnavailable = true
        // status = CarDataStatus.Available
        _carDataInfoState.update {
            it.copy(status = CarDataStatus.Loading)
        }
        val vehicles = mutableListOf<CarDataInfo.CarData>()
        for (api in AvailableApis.list) {
            selectApi(api).getVehicles(context)?.let {
                vehicles.addAll(it)
            }
        }

        if (networkState.value.connected == true) {
            _carDataInfoState.update {
                it.copy(
                    carData = vehicles,
                    status = CarDataStatus.Available,
                    message = ""
                )
            }
        } else {
            _carDataInfoState.update {
                it.copy(
                    status = CarDataStatus.Unavailable,
                    message = networkState.value.message
                )
            }
        }


        return vehicles
    }

    fun selectApi(api: AvailableApis): CarDataInterface {
        return when (api) {
            AvailableApis.Polestar -> PolestarRepository
            AvailableApis.Tibber -> TibberRepository
        }
    }
}