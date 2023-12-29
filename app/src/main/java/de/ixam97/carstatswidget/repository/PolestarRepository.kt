package de.ixam97.carstatswidget.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.impl.model.systemIdInfo
import de.ixam97.carstatswidget.PreferencesManager
import de.ixam97.carstatswidget.RetrofitInstance
import de.ixam97.carstatswidget.repository.responses.PolestarBatteryData
import de.ixam97.carstatswidget.repository.responses.PolestarGetCars
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

object PolestarRepository: CarDataInterface() {
    private const val TAG = "PolestarRepository"
    private var token: PolestarToken? = null
    private var credentials: ApiCredentials? = null

    override val mailKey: String = "polestarMail"
    override val passwordKey: String = "polestarPassword"
    override val name: String = "Polestar"

    private val _polestarDataState = MutableStateFlow(PolestarData())
    val polestarDataStatus = _polestarDataState.asStateFlow()

    data class LoginFlowTokens(
        val pathToken: String,
        val cookie: String
    )

    override suspend fun verifyLogin(context: Context): Boolean {
        // val tokenExpired
        if (token != null) return true

        val preferencesManager = PreferencesManager(context = context)

        val storedCredentials = ApiCredentials(
            email = preferencesManager.getString(mailKey, ""),
            password = preferencesManager.getString(passwordKey, "")
        )

        if (storedCredentials.email == "" || storedCredentials.password == "") return false

        return login(storedCredentials, context)
    }

    override suspend fun logout(context: Context) {
        token = null
        credentials = null
        val preferencesManager = PreferencesManager(context = context)
        preferencesManager.saveString(mailKey, "")
        preferencesManager.saveString(passwordKey, "")
    }

    override suspend fun login(apiCredentials: ApiCredentials, context: Context): Boolean {
        CarDataRepository.startNetworkAttempt()
        val loginFlowTokens = getLoginFlowTokens()?: return false
        val tokenRequestCode = getTokenRequestCode(credentials = apiCredentials, loginFlowTokens = loginFlowTokens)?: return false
        val token = getBearerToken(tokenRequestCode)
        this.token = token
        if (token != null) {
            val preferencesManager = PreferencesManager(context = context)
            preferencesManager.saveString(mailKey, apiCredentials.email)
            preferencesManager.saveString(passwordKey, apiCredentials.password)
            return true
        }
        return false
    }

    override suspend fun getVehicles(context: Context): List<CarDataInfo.CarData>? {

        var currentToken = token ?: return null
        CarDataRepository.startNetworkAttempt()
        if (!checkTokenValidity()) {
            refreshToken()
            if (!verifyLogin(context)) return null
            currentToken = token ?: return null
        }

        var cars: PolestarGetCars? = null
        val vehicles = mutableListOf<CarDataInfo.CarData>()

        try {
            withContext(Dispatchers.IO) {
                RetrofitInstance.polestarDataApi.run {
                    val response = getCars(auth = currentToken.accessToken)
                    cars = response.body()
                }
            }

            if (cars == null) return null


            for (car in cars!!.data.getConsumerCarsV2) {
                var batteryData: PolestarBatteryData? = null
                withContext(Dispatchers.IO) {
                    RetrofitInstance.polestarDataApi.run {
                        val response = getBatteryData(
                            auth = currentToken.accessToken,
                            body = GetBatteryDataBody(
                                variables = VinVariables(car.vin)
                            )
                        )

                        batteryData = response.body()
                    }
                }

                if (batteryData != null) {
                    val data = batteryData!!.data.getBatteryData
                    vehicles.add(
                        CarDataInfo.CarData(
                            stateOfCharge = data.batteryChargeLevelPercentage,
                            lastSeen = data.eventUpdatedTimestamp.iso,
                            lastUpdated = DateFormat.getDateTimeInstance().format(Date(System.currentTimeMillis())),
                            imgUrl = car.content.images.studio.url + "&width=1400&angle=1&bg=00000000",
                            name = car.content.model.name,
                            shortName = car.content.model.name,
                            id = car.vin,
                            api = name
                        )
                    )
                }
            }

            Log.i(TAG, cars.toString())
            CarDataRepository.resetNetworkError()
        } catch (e: Exception) {
            handleException(e, TAG)
            return null
        }
        // status = CarDataStatus.Available

        // Log.i(TAG, vehicles.toString())

        return vehicles
    }

    private suspend fun checkTokenValidity(): Boolean {
        if (token == null) return false
        Log.d(TAG, "Validating Token")

        val expiresSoon = token!!.expirationTime.time - System.currentTimeMillis() < 600_000 // Refresh access token 10 minutes before becoming invalid // (1000 * 60 * 60 - 30_000)

        if (expiresSoon) {
            Log.d(TAG, "Token expiring soon")
            return false
        }

        var tokenValid: Boolean = false
        withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.polestarDataApi.run {
                    val response = checkTokenValidity(
                        // variables = refreshTokenVariables(token!!.accessToken),
                        body = ValidateTokenBody(variables = TokenVariables(token!!.accessToken)),
                        auth = "Bearer ${token!!.accessToken}"
                    )
                    Log.d(TAG, response.toString())
                    val result = response.body() ?: throw Exception("Request to validate Token failed!")
                    Log.d(TAG, result.toString())
                    if (result.data.introspectToken == null) return@withContext
                    tokenValid = result.data.introspectToken.active
                }
                CarDataRepository.resetNetworkError()
            } catch (e: Exception) {
                handleException(e, TAG)
            }
        }

        Log.d(TAG, "Token valid: $tokenValid")

        return tokenValid
    }

    private suspend fun refreshToken(): PolestarToken? {
        if (token == null) return null
        Log.d(TAG, "Refreshing Token...")
        var polestarToken: PolestarToken? = null
        withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.polestarDataApi.run {
                    val response = refreshToken(
                        // variables = refreshTokenVariables(token!!.refreshToken),
                        body = RefreshTokenBody(variables = TokenVariables(token!!.refreshToken)),
                        auth = "Bearer ${token!!.accessToken}"
                    )
                    Log.d(TAG, response.toString())
                    val result = response.body()?: throw Exception("Request to refresh Token failed!")
                    Log.d(TAG, result.toString())
                    result.data.refreshAuthToken ?: throw Exception("Request to refresh Token failed!")
                    polestarToken = PolestarToken(
                        accessToken = result.data.refreshAuthToken.access_token,
                        refreshToken = result.data.refreshAuthToken.refresh_token,
                        expirationTime = Date(System.currentTimeMillis() + result.data.refreshAuthToken.expires_in * 1000)
                    )
                }
                token = polestarToken
                CarDataRepository.resetNetworkError()
            } catch (e: Exception) {
                handleException(e, TAG)
                if (e !is java.net.UnknownHostException) {
                    token = null
                }
            }
        }

        // token = polestarToken

        if (token == null) {
            Log.w(TAG, "Token cannot be refreshed. New Login required.")
            return null
        }

        Log.d(TAG, "Bearer Token: ${token!!.accessToken}")
        Log.d(TAG, "Refresh Token: ${token!!.refreshToken}")
        Log.d(TAG, "Expires at: ${token!!.expirationTime} (current: ${Date()})")


        return polestarToken
    }

    private suspend fun getLoginFlowTokens(): LoginFlowTokens? {
        var pathToken: String? = null
        var cookie: String? = null
        withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.polestarAuthApi.run {
                    val response = getLoginFlowTokens()
                    val url = Uri.parse(response.headers()["location"])
                    cookie = response.headers()["set-cookie"].toString().substringBefore("; ")
                    pathToken = url.getQueryParameter("resumePath")
                }
                CarDataRepository.resetNetworkError()
            } catch(e: Exception) {
                handleException(e, TAG)
            }
        }

        Log.d(TAG, "PathToken: $pathToken")
        Log.d(TAG, "Cookie: $cookie")
        return if (pathToken == null || cookie == null) null else LoginFlowTokens(pathToken = pathToken!!, cookie = cookie!!)
    }

    private suspend fun getTokenRequestCode(credentials: ApiCredentials, loginFlowTokens: LoginFlowTokens): String? {
        var tokenRequestCode: String? = null

        withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.polestarAuthApi.run {
                    val result = performLogin(
                        pathToken = loginFlowTokens.pathToken,
                        cookie = loginFlowTokens.cookie,
                        email = credentials.email,
                        password = credentials.password
                    )
                    Log.d(TAG, result.toString())
                    val url = Uri.parse(result.raw().headers["location"])
                    tokenRequestCode = url.getQueryParameter("code")
                }
                CarDataRepository.resetNetworkError()
            } catch (e: Exception) {
                handleException(e, TAG)
            }
        }

        Log.d(TAG, "TokenRequestCode: $tokenRequestCode")

        return tokenRequestCode
    }

    private suspend fun getBearerToken(tokenRequestCode: String): PolestarToken? {
        var polestarToken: PolestarToken? = null

        withContext(Dispatchers.IO) {
            try {
                RetrofitInstance.polestarDataApi.run {
                    // val variablesParameter = "%7B%22code%22%3A%22${tokenRequestCode}%22%7D"
                    val response = getBearerToken(body = GetBearerTokenBody(variables =  getBearerTokenVariables(tokenRequestCode)))
                    // val response = getBearerToken(variablesParameter)
                    Log.d(TAG, response.toString())
                    val result = response.body() ?: throw Exception("Request for Bearer Token failed!")
                    polestarToken = PolestarToken(
                        accessToken = result.data.getAuthToken.access_token,
                        refreshToken = result.data.getAuthToken.refresh_token,
                        expirationTime = Date(System.currentTimeMillis() + result.data.getAuthToken.expires_in * 1000)
                    )
                }
                CarDataRepository.resetNetworkError()
            } catch (e: Exception) {
                handleException(e, TAG)
            }
        }

        if (polestarToken == null) return null

        Log.d(TAG, "Bearer Token: ${polestarToken!!.accessToken}")
        Log.d(TAG, "Refresh Token: ${polestarToken!!.refreshToken}")
        Log.d(TAG, "Expires at: ${polestarToken!!.expirationTime} (current: ${Date()})")

        return polestarToken
    }

    private fun refreshTokenVariables(token: String) = "{\"token\":\"$token\"}"
    private fun getBearerTokenVariables(code: String) = "{\"code\":\"$code\"}"

    data class TokenVariables(
        val token: String
    )

    data class VinVariables(
        val vin: String
    )

    data class GetBearerTokenBody(
        val query: String = "query getAuthToken(\$code: String!) {getAuthToken(code: \$code) {id_token access_token refresh_token expires_in}}",
        val operationName: String = "getAuthToken",
        val variables: String
    )

    data class RefreshTokenBody(
        val query: String = "query refreshAuthToken(\$token: String!) {refreshAuthToken(token: \$token) {access_token expires_in id_token refresh_token}}",
        val variables: TokenVariables
    )

    data class ValidateTokenBody(
        val query: String = "query introspectToken(\$token: String!) {introspectToken(token: \$token) {active}}",
        val operationName: String = "introspectToken",
        val variables: TokenVariables
    )

    data class GetCarsBody(
        val query: String = "query getCars { getConsumerCarsV2 { vin content { model { code name } images { studio { url angles }}}}}",
        val operationName: String = "getCars",
        val variables: String = "{}"
    )

    data class GetBatteryDataBody(
        val query: String = "query GetBatteryData(\$vin: String!) { getBatteryData(vin: \$vin) { batteryChargeLevelPercentage chargerConnectionStatus chargingStatus eventUpdatedTimestamp { iso } estimatedDistanceToEmptyKm }}",
        val variables: VinVariables
    )
}