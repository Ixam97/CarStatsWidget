package de.ixam97.carstatswidget.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import de.ixam97.carstatswidget.BuildConfig
import de.ixam97.carstatswidget.CarStatsWidget
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.TibberRepository
import de.ixam97.carstatswidget.repository.CarDataStatus
import de.ixam97.carstatswidget.repository.CarDataWorker
import de.ixam97.carstatswidget.repository.CarDataRepository
import de.ixam97.carstatswidget.repository.ApiCredentials
import de.ixam97.carstatswidget.repository.PolestarRepository
import de.ixam97.carstatswidget.util.AvailableApis
import de.ixam97.carstatswidget.util.SemanticVersion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application = application) {

    companion object {
        private const val TAG = "MainViewModel"
    }
/*
    val preferencesManager = de.ixam97.carstatswidget.PreferencesManager(context = application)
    val workManager = WorkManager.getInstance(application)

    val manager = GlanceAppWidgetManager(application)

 */
    val applicationContext = application

    private val _globalState = MutableStateFlow(GlobalState())
    val globalState: StateFlow<GlobalState> = _globalState.asStateFlow()

    data class GlobalState(
        val isLoggedIn: Boolean? = null,
        val loggedInApis: List<AvailableApis> = listOf(),
        val updateAvailable: Boolean? = null,
        val currentVersion: String = BuildConfig.VERSION_NAME,
        val availableVersion: String? = null
    )

    private val _tibberLoginState = MutableStateFlow(TibberLoginState())
    val tibberLoginState: StateFlow<TibberLoginState> = _tibberLoginState.asStateFlow()

    data class TibberLoginState(
        val loginPossible: Boolean = false,
        val loginFailed: Boolean = false,
        val passwordVisible: Boolean = false
    )

    data class PolestarLoginState(
        val loginPossible: Boolean = false,
        val loginFailed: Boolean = false,
        val passwordVisible: Boolean = false
    )

    private val _carInfoState = MutableStateFlow(CarInfoState())
    val carInfoState: StateFlow<CarInfoState> = _carInfoState.asStateFlow()

    private val _carDataState = MutableStateFlow<List<CarDataInfo.CarData>>(emptyList())
    val carDataState: StateFlow<List<CarDataInfo.CarData>> = _carDataState.asStateFlow()

    private val _networkState = MutableStateFlow<CarDataRepository.NetworkState>(CarDataRepository.NetworkState())
    val networkState = _networkState.asStateFlow()

    data class CarInfoState(
        val carDataInfo: CarDataInfo = CarDataInfo(CarDataStatus.Unavailable, message = "No Data"),
        val dataAvailable: Boolean = false,
        val dataRequested: Boolean = false
    )
/*
    var tibberMail by mutableStateOf(preferencesManager.getString("tibberMail", ""))
        private set

    var tibberPassword by mutableStateOf(preferencesManager.getString("tibberPassword", ""))
        private set

 */

    init {
        Log.i(TAG, "Init")
        requestCarData(initial = true)

        /*
        if (tibberMail != "" && tibberPassword != "") {
            _tibberLoginState.update {
                it.copy(loginPossible = true)
            }
            // verifyLogin()
            _globalState.update {
                it.copy(isLoggedIn = true)
            }
        } else {
            _globalState.update {
                it.copy(isLoggedIn = false)
            }
        }
        */
        viewModelScope.launch {
            // TibberRepository.carDataInfoState.collect { carDataInfo ->
            CarDataRepository.carDataInfoState.collect { carDataInfo ->
                Log.d("ViewModel", "Car data status: ${carDataInfo.status}")
                _carInfoState.update { carInfoState ->
                    validateLogins()
                    carInfoState.copy(carDataInfo = carDataInfo)
                }
                if (carDataInfo.status == CarDataStatus.Available) {
                    _carDataState.update {
                        carInfoState.value.carDataInfo.carData
                    }
                }
            }
        }

        viewModelScope.launch {
            CarDataRepository.networkState.collect { networkState ->
                _networkState.update { networkState }
            }
        }

        viewModelScope.launch {
            getApplication<CarStatsWidget>().gitHubVersionStateFlow.collect {gitHubVersion ->
                val currentVersion = BuildConfig.VERSION_NAME
                Log.d(TAG, "Current version: $currentVersion, latest Version: $gitHubVersion")
                _globalState.update {
                    it.copy(
                        updateAvailable = if (gitHubVersion == null) null else SemanticVersion.compareVersions(
                            v1 = SemanticVersion(currentVersion),
                            v2 = SemanticVersion(gitHubVersion)
                        ),
                        availableVersion = gitHubVersion
                    )
                }
            }
        }
        requestCarData()
    }
/*
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
*/
    fun requestCarData(initial: Boolean = false) {
        if (networkState.value.connected == false || initial) {
            viewModelScope.launch {
                validateLogins().join()
                CarDataWorker.enqueue(applicationContext, true)
            }
        } else {
            CarDataWorker.enqueue(applicationContext, true)
        }
    }

    fun checkForUpdates() {
        (applicationContext as CarStatsWidget).updateGitHubVersion()
    }

    fun validateLogins(): Job {
        _globalState.update {
            it.copy(isLoggedIn = null)
        }
        return viewModelScope.launch {
            _globalState.update {
                Log.i(TAG, "Checking logged in APIs")
                val loggedInApis = CarDataRepository.getLoggedInApis(applicationContext)
                it.copy(
                    loggedInApis = loggedInApis,
                    isLoggedIn = loggedInApis.isNotEmpty()
                )
            }
        }
    }

    fun logoutApi(api: AvailableApis) {
        viewModelScope.launch {
            CarDataRepository.logout(api, applicationContext)
            validateLogins().join()
        }
    }

    // fun polestarTest() {
    //     viewModelScope.launch {
    //         val credentials = ApiCredentials(
    //             "maxigoldschmidt@gmail.com",
    //             "7PBX*GqE8xGXpvM"
    //         )
    //         val bearerToken = PolestarRepository.login(credentials)
    //         delay(2000)
    //         PolestarRepository.checkTokenValidity()
    //         delay(2000)
    //         PolestarRepository.refreshToken()
    //     }
    // }
/*
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
            if (loginSuccess) {
                _globalState.update {
                    it.copy(isLoggedIn = true)
                }
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
*/
}