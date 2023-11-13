package de.ixam97.carstatswidget.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import de.ixam97.carstatswidget.StateOfChargeWidget
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.CarDataInfoStateDefinition
import de.ixam97.carstatswidget.repository.CarDataRepository
import de.ixam97.carstatswidget.repository.CarDataWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application = application) {

    val preferencesManager = de.ixam97.carstatswidget.PreferencesManager(context = application)
    val workManager = WorkManager.getInstance(application)

    val manager = GlanceAppWidgetManager(application)
    val applicationContext = application

    private val _globalState = MutableStateFlow(GlobalState())
    val globalState: StateFlow<GlobalState> = _globalState.asStateFlow()

    data class GlobalState(
        val isLoggedIn: Boolean? = null,
        val showLastSeen: Boolean? = null,
        val showVehicleName: Boolean? = null
    )

    private val _tibberLoginState = MutableStateFlow(TibberLoginState())
    val tibberLoginState: StateFlow<TibberLoginState> = _tibberLoginState.asStateFlow()

    data class TibberLoginState(
        val loginPossible: Boolean = false,
        val loginFailed: Boolean = false,
        val passwordVisible: Boolean = false
    )

    private val _carInfoState = MutableStateFlow(CarInfoState())
    val carInfoState: StateFlow<CarInfoState> = _carInfoState.asStateFlow()

    data class CarInfoState(
        val carDataInfo: CarDataInfo = CarDataInfo.Unavailable("No Data"),
        val dataAvailable: Boolean = false,
        val dataRequested: Boolean = false
    )

    var tibberMail by mutableStateOf(preferencesManager.getString("tibberMail", ""))
        private set

    var tibberPassword by mutableStateOf(preferencesManager.getString("tibberPassword", ""))
        private set

    init {
        if (tibberMail != "" && tibberPassword != "") {
            _tibberLoginState.update {
                it.copy(loginPossible = true)
            }
            verifyLogin()
        } else {
            _globalState.update {
                it.copy(isLoggedIn = false)
            }
        }
        _globalState.update {
            it.copy(
                showLastSeen = preferencesManager.getBoolean("showLastSeen", true),
                showVehicleName = preferencesManager.getBoolean("showVehicleName", true)
            )
        }
        viewModelScope.launch {
            CarDataRepository.carDataInfoFlow.collect { carDataInfo ->
                _carInfoState.update { carInfoState ->
                    carInfoState.copy(carDataInfo = carDataInfo)
                }
            }
        }
        requestCarData()
    }

    fun mailEntered(mail: String) {
        this.tibberMail = mail
        checkValidInputs()
    }

    fun passwordEntered(password: String) {
        this.tibberPassword = password
        checkValidInputs()
    }

    fun passwordHideToggle() {
        _tibberLoginState.update {
            it.copy(passwordVisible = !_tibberLoginState.value.passwordVisible)
        }
    }

    fun loginPressed() {
        if (_tibberLoginState.value.loginPossible) {
            verifyLogin()
        }
    }

    fun logoutPressed() {
        if (_globalState.value.isLoggedIn == true) {
            logout()
        }
    }

    fun requestCarData() {
        CarDataWorker.enqueue(applicationContext, true)
    }

    fun setShowLastSeen(checked: Boolean) {
        preferencesManager.saveBoolean("showLastSeen", checked)
        _globalState.update {
            it.copy(showLastSeen = checked)
        }
        if (carInfoState.value.carDataInfo is CarDataInfo.Available) {
            _carInfoState.update {
                it.copy(
                    carDataInfo = (it.carDataInfo as CarDataInfo.Available).copy(
                        showLastSeen = checked
                    )
                )
            }
        }
        refreshWidgets(applicationContext, carInfoState.value.carDataInfo)
    }

    fun setShowVehicleName(checked: Boolean) {
        preferencesManager.saveBoolean("showVehicleName", checked)
        _globalState.update {
            it.copy(showVehicleName = checked)
        }
        if (carInfoState.value.carDataInfo is CarDataInfo.Available) {
            _carInfoState.update {
                it.copy(
                    carDataInfo = (it.carDataInfo as CarDataInfo.Available).copy(
                        showVehicleName = checked
                    )
                )
            }
        }
        refreshWidgets(applicationContext, carInfoState.value.carDataInfo)
    }

    private fun checkValidInputs() {
        val validInputs = tibberMail.isNotEmpty() && tibberPassword.isNotEmpty()

        _tibberLoginState.update {
            it.copy(loginPossible = validInputs)
        }
    }

    private fun verifyLogin() {
        viewModelScope.launch {
            val loginSuccess = CarDataRepository.verifyLoginData(tibberMail, tibberPassword)
            _tibberLoginState.update {
                it.copy(loginFailed = !loginSuccess)
            }
            _globalState.update {
                it.copy(isLoggedIn = loginSuccess)
            }
            if (loginSuccess) {
                preferencesManager.saveString("tibberMail", tibberMail)
                preferencesManager.saveString("tibberPassword", tibberPassword)
                CarDataWorker.enqueue(context = applicationContext, force = true)
                Log.d("ViewModel", "SavedCredentials: ${preferencesManager.getString("tibberMail", "")}")
            }
        }
    }

    private fun logout() {
        _tibberLoginState.update {
            it.copy(loginFailed = false)
        }
        tibberMail = ""
        tibberPassword = ""
        preferencesManager.saveString("tibberMail", tibberMail)
        preferencesManager.saveString("tibberPassword", tibberPassword)
        _globalState.update {
            it.copy(isLoggedIn = false)
        }
    }

    private fun refreshWidgets(context: Context, newState: CarDataInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val glanceIds = manager.getGlanceIds(StateOfChargeWidget::class.java)
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
    }
}