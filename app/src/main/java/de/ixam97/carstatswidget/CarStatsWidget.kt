package de.ixam97.carstatswidget

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import de.ixam97.carstatswidget.repository.CarDataInfo
import de.ixam97.carstatswidget.repository.CarDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CarStatsWidget: Application() {

    companion object {
        private const val TAG = "CarStatsWidget"
    }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            CarDataRepository.carDataInfoFlow.collect { carDataInfo ->
                if (carDataInfo is CarDataInfo.Unavailable) {
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
        /*
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, WidgetUpdateReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(TAG, "Setting up Alarm Manager")
        // sendBroadcast(Intent(applicationContext, WidgetUpdateReceiver::class.java))
        alarmManager.setRepeating(
            AlarmManager.RTC,
            SystemClock.elapsedRealtime() + 10_000,
            30_000,
            pendingIntent
            )

         */
    }
}