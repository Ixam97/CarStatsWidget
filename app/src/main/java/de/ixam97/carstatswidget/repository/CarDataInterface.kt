package de.ixam97.carstatswidget.repository

import android.content.Context
import android.util.Log

abstract class CarDataInterface {
    abstract val passwordKey: String
    abstract val mailKey: String
    abstract val name: String

    // final var status: CarDataStatus = CarDataStatus.NotLoggedIn
    //     protected set
    // final var message: String = ""
    //     protected set

    var networkStatus: Boolean? = null
        protected set

    abstract suspend fun login(apiCredentials: ApiCredentials, context: Context): Boolean

    abstract suspend fun logout(context: Context)

    abstract suspend fun verifyLogin(context: Context): Boolean

    abstract suspend fun getVehicles(context: Context): List<CarDataInfo.CarData>?

    protected fun handleException(e: Exception, tag: String) {
        if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
            // status = CarDataStatus.Unavailable
            // message = "Network connection error"
            Log.e(tag, "Network connection error: ${e.message}")
            CarDataRepository.setNetworkError(e.message?:"Unknown error")
        } else {
            Log.e(tag, e.stackTraceToString())
        }
    }
}

data class ApiCredentials(
    val email: String,
    val password: String
)