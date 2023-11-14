package de.ixam97.carstatswidget.repository

import android.util.Log
import de.ixam97.carstatswidget.RetrofitInstance
import de.ixam97.carstatswidget.repository.tibberCredentials.TibberCredentials
import de.ixam97.carstatswidget.repository.tibberData.TibberData
import de.ixam97.carstatswidget.repository.tibberQuery.TibberQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

object CarDataRepository {
    private const val TAG = "CarDataRepository"

    private val _carDataInfoFlow = MutableSharedFlow<CarDataInfo>()
    val carDataInfoFlow: SharedFlow<CarDataInfo> = _carDataInfoFlow.asSharedFlow()

    suspend fun verifyLoginData(mail: String, password: String): Boolean {
        var verifyResponse: Boolean = false
        withContext(Dispatchers.IO) {
            de.ixam97.carstatswidget.RetrofitInstance.tibberApi.run {
                val tibberCredentials = TibberCredentials(mail, password)
                try {
                    verifyResponse = (authenticateTibber(tibberCredentials).body()?.token?:"") != ""
                } catch (e: Exception) {
                    Log.e(TAG, "Query failed: \n$e")
                }
            }
        }
        return verifyResponse
    }

    suspend fun getCarDataInfo(email: String, password: String): CarDataInfo {
        _carDataInfoFlow.emit(CarDataInfo.Loading)
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

        val returnData = if (dataResponse != null) {
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
            CarDataInfo.Available(carData = mutableCarData.toList())
        } else {
            CarDataInfo.Unavailable(errorMessage)
        }

        _carDataInfoFlow.emit(returnData)

        return returnData
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
}