package de.ixam97.carstatswidget.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.ixam97.carstatswidget.PreferencesManager
import de.ixam97.carstatswidget.repository.TibberRepository
import de.ixam97.carstatswidget.repository.ApiCredentials
import de.ixam97.carstatswidget.repository.CarDataRepository
import de.ixam97.carstatswidget.repository.PolestarRepository
import de.ixam97.carstatswidget.util.AvailableApis
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application = application)  {
    private val TAG = "LoginViewModel"

    private val preferencesManager = PreferencesManager(application)

    private val context: Context = application

    var mail: String by mutableStateOf("")
        private set
    var pass: String by mutableStateOf("")
        private set

    data class LoginState(
        val loginPossible: Boolean = false,
        val loginFailed: Boolean = false,
        val passwordVisible: Boolean = false
    )

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    private val _popBackstack = Channel<Boolean>(Channel.BUFFERED)
    val popBackstack = _popBackstack.receiveAsFlow()

    var selectedApi: AvailableApis? = null

    fun mailEntered(mail: String) {
        this.mail = mail
        checkValidInputs()
    }

    fun passwordEntered(password: String) {
        this.pass = password
        checkValidInputs()
    }

    fun passwordHideToggle() {
        _loginState.update {
            it.copy(passwordVisible = !_loginState.value.passwordVisible)
        }
    }

    fun loginPressed() {
        if (_loginState.value.loginPossible) {
            viewModelScope.launch {
                selectedApi?.let {selectedApi ->
                    val loginResult = CarDataRepository.login(
                        api = selectedApi,
                        credentials = ApiCredentials(mail, pass),
                        context = context
                    )

                    if (loginResult) {
                        _popBackstack.send(true)
                    } else {
                        _loginState.update {
                            it.copy(loginFailed = true)
                        }
                    }
                }

            }
            /*
            when (selectedApi) {
                AvailableApis.Tibber -> {
                    viewModelScope.launch {
                        if (TibberRepository.verifyLoginData(mail, pass)) {
                            preferencesManager.saveString("tibberMail", mail)
                            preferencesManager.saveString("tibberPassword", pass)
                        } else {
                            _loginState.update {
                                it.copy(loginFailed = true)
                            }
                        }
                    }
                }
                AvailableApis.Polestar -> {
                    viewModelScope.launch {
                        val credentials = ApiCredentials(mail, pass)
                        if (PolestarRepository.login(credentials) != null) {
                            preferencesManager.saveString("polestarMail", mail)
                            preferencesManager.saveString("polestarPassword", pass)
                        } else {
                            _loginState.update {
                                it.copy(loginFailed = true)
                            }
                        }
                    }
                }
                else -> {

                }
            }

             */
        }
    }

    private fun checkValidInputs() {
        val validInputs = mail.isNotEmpty() && pass.isNotEmpty()

        _loginState.update {
            it.copy(loginPossible = validInputs)
        }
    }
}