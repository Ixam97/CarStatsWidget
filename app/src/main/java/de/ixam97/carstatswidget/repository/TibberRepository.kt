package de.ixam97.carstatswidget.repository

import android.content.Context
import android.util.Log
import de.ixam97.carstatswidget.PreferencesManager
import de.ixam97.carstatswidget.RetrofitInstance
import de.ixam97.carstatswidget.repository.responses.TibberCredentials
import de.ixam97.carstatswidget.repository.responses.TibberData
import de.ixam97.carstatswidget.repository.tibberQuery.TibberQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

object TibberRepository: CarDataInterface() {
    private const val TAG = "TibberRepository"

    private var credentials: ApiCredentials? = null
    private var bearerToken: String? = null

    override val mailKey: String = "tibberMail"
    override val passwordKey: String = "tibberPassword"
    override val name: String = "Tibber"

    // private val _carDataInfoState = MutableStateFlow<CarDataInfo>(CarDataInfo(CarDataStatus.Unavailable))
    // private val carDataInfoState: StateFlow<CarDataInfo> = _carDataInfoState.asStateFlow()

    // override var status: CarDataStatus? = null
    // override var message: String = ""

    override suspend fun verifyLogin(context: Context): Boolean {
        if (bearerToken != null) return true

        val preferencesManager = PreferencesManager(context = context)

        val storedCredentials = credentials ?: ApiCredentials(
            email = preferencesManager.getString(mailKey, ""),
            password = preferencesManager.getString(passwordKey, "")
        )

        return login(storedCredentials, context)
    }

    override suspend fun logout(context: Context) {
        credentials = null
        bearerToken = null
        val preferencesManager = PreferencesManager(context = context)
        preferencesManager.saveString(mailKey, "")
        preferencesManager.saveString(passwordKey, "")
    }

    override suspend fun login(apiCredentials: ApiCredentials, context: Context): Boolean {
        val tibberCredentials = TibberCredentials(apiCredentials.email, apiCredentials.password)

        if (tibberCredentials.email == "" || tibberCredentials.password == "") return false

        CarDataRepository.startNetworkAttempt()

        var verifyResponse = false
        var newToken: String = ""

        withContext(Dispatchers.IO) {
            RetrofitInstance.tibberApi.run {
                try {
                    newToken = authenticateTibber(tibberCredentials).body()?.token?:""
                    verifyResponse = newToken != ""
                    CarDataRepository.resetNetworkError()
                } catch (e: Exception) {
                    handleException(e, TAG)
                }
            }
        }

        if (verifyResponse) {
            bearerToken = newToken
            credentials = apiCredentials
            val preferencesManager = PreferencesManager(context = context)
            preferencesManager.saveString(mailKey, apiCredentials.email)
            preferencesManager.saveString(passwordKey, apiCredentials.password)
        }

        return verifyResponse
    }

    override suspend fun getVehicles(context: Context): List<CarDataInfo.CarData>? {
        CarDataRepository.startNetworkAttempt()
        var data: TibberData? = null
        val vehicles = mutableListOf<CarDataInfo.CarData>()

        try {
            withContext(Dispatchers.IO) {
                RetrofitInstance.tibberApi.run {
                    var response = fetchTibberData(
                        auth = "Bearer $bearerToken",
                        query = TibberQuery()
                    )

                    if (response.code() == 401) {
                        if (!verifyLogin(context)) return@run
                        else {
                            response =  fetchTibberData(
                                auth = "Bearer $bearerToken",
                                query = TibberQuery()
                            )
                        }
                    }
                    data = response.body()
                }
            }



            if (data != null) {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val localeTimeDateFormat = DateFormat.getDateTimeInstance()
                val lastUpdateDate = Date(System.currentTimeMillis())

                for (car in data!!.data.me.homes[0].electricVehicles) {
                    val lastSeenDate = simpleDateFormat.parse(car.lastSeen)!!

                    vehicles.add(
                        CarDataInfo.CarData(
                            stateOfCharge = car.battery.percent,
                            lastSeen = localeTimeDateFormat.format(lastSeenDate),
                            lastUpdated = localeTimeDateFormat.format(lastUpdateDate),
                            imgUrl = car.imgUrl,
                            name = car.name,
                            shortName = car.shortName,
                            id = car.id,
                            api = name
                        )
                    )
                }
            }
            CarDataRepository.resetNetworkError()

        } catch (e: Exception) {
            handleException(e, TAG)
            return null
        }
        // status = CarDataStatus.Available

        return vehicles
    }

    suspend fun getGitHubVersion(): String {
        var version: String = "0.0.0"
        withContext(Dispatchers.IO) {
            RetrofitInstance.gitHubVersionChecker.run {
                try {
                    // Log.d(TAG, "URL: ${fetchGitHubVersion().raw().request.url}")
                    version = fetchGitHubVersion().raw().request.url.toString().split("/v").run {
                        if (this.size > 1) {
                            this.last()
                        } else {
                            version
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Query failed: \n$e")
                }
            }
        }
        return version
    }

    /*
    suspend fun getCarDataInfo(email: String, password: String): CarDataInfo {
        // _carDataInfoState.update {
        //     it.copy(
        //         status = CarDataStatus.Loading
        //     )
        // }
        var dataResponse: TibberData? = null
        var errorMessage: String = "Unknown Error"
        withContext(Dispatchers.IO) {
            RetrofitInstance.tibberApi.run {
                val tibberCredentials = TibberCredentials(email, password)
                try {
                    val auth = authenticateTibber(tibberCredentials)
                    Log.d(TAG, "Auth code: ${auth.code()}: ${auth.message()}, ${auth.body()}")
                    if (auth.code() != 200) throw httpException(auth.code())
                    val token = auth.body()?.token?:""
                    dataResponse = fetchTibberData(
                        auth = "Bearer $token",
                        query = TibberQuery("{me {homes {electricVehicles {id lastSeen name shortName battery {percent isCharging} imgUrl}}}}")
                    ).body()
                } catch (e: Exception) {
                    Log.e(TAG, "Query failed: \n$e")
                    errorMessage = e.localizedMessage?: "Unknown error"
                }

                if (dataResponse != null) {
                    Log.i(TAG, dataResponse.toString())
                }
            }
        }

        if (dataResponse != null) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val localeTimeDateFormat = DateFormat.getDateTimeInstance()
            val lastUpdateDate = Date(System.currentTimeMillis())

            val mutableCarData = mutableListOf<CarDataInfo.CarData>()

            for (electricVehicle in dataResponse!!.data.me.homes[0].electricVehicles) {

                val lastSeenDate = simpleDateFormat.parse(electricVehicle.lastSeen)!!

                Log.d(TAG, "Last seen: ${localeTimeDateFormat.format(lastSeenDate)}")
                Log.d(TAG, "Last updated: ${localeTimeDateFormat.format(lastUpdateDate)}")

                mutableCarData.add(
                    CarDataInfo.CarData(
                        stateOfCharge = electricVehicle.battery.percent,
                        lastSeen = localeTimeDateFormat.format(lastSeenDate),
                        lastUpdated = localeTimeDateFormat.format(lastUpdateDate),
                        imgUrl = electricVehicle.imgUrl,
                        name = electricVehicle.name,
                        shortName = electricVehicle.shortName,
                        id = electricVehicle.id
                    )
                )
            }
            _carDataInfoState.update {
                it.copy(
                    status = CarDataStatus.Available,
                    carData = mutableCarData.toList()
                )
            }
        } else {
            _carDataInfoState.update {
                it.copy(
                    status = CarDataStatus.Unavailable,
                    message = errorMessage
                )
            }
        }

        return carDataInfoState.value
    }

    fun setNotLoggedIn() {
        _carDataInfoState.update {
            it.copy(
                status = CarDataStatus.NotLoggedIn
            )
        }
    }

    fun httpException(code: Int): Exception {
        val message = when (code) {
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"

            else -> "Unknown code"
        }
        return Exception("HTTP Code $code: $message")
    }
    */
}