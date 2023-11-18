package de.ixam97.carstatswidget

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import de.ixam97.carstatswidget.repository.CarDataRepository
import de.ixam97.carstatswidget.repository.CarDataStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CarStatsWidget: Application() {

    companion object {
        private const val TAG = "CarStatsWidget"
    }

    // var gitHubVersion: String? = null
    private val _gitHubVersionStateFlow = MutableStateFlow<String?>(null)
    val gitHubVersionStateFlow = _gitHubVersionStateFlow.asStateFlow()

    fun updateGitHubVersion() {
        CoroutineScope(Dispatchers.IO).launch {
            _gitHubVersionStateFlow.update {
                null
            }
            val gitHubVersion = CarDataRepository.getGitHubVersion()
            _gitHubVersionStateFlow.update {
                gitHubVersion
            }
            // Log.d(TAG, "GitHub version: $gitHubVersion")
        }
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            CarDataRepository.carDataInfoState.collect { carDataInfo ->
                if (carDataInfo.status == CarDataStatus.Unavailable) {
                    val alarmManager =
                        applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        Intent(applicationContext, WidgetUpdateReceiver::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    Log.d(TAG, "Reattempting Tibber fetch")
                    // sendBroadcast(Intent(applicationContext, WidgetUpdateReceiver::class.java))
                    alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + 10_000,
                        pendingIntent
                    )
                }
            }
        }
        updateGitHubVersion()

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, WidgetUpdateReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(TAG, "Setting up Alarm Manager")
        // sendBroadcast(Intent(applicationContext, WidgetUpdateReceiver::class.java))
        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + 60_000,
            // 60_000,
            pendingIntent
            )
    }
}