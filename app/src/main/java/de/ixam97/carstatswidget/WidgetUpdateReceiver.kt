package de.ixam97.carstatswidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.ixam97.carstatswidget.repository.CarDataWorker

class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetUpdateReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Enqueueing Worker from Broadcast...")
        CarDataWorker.enqueue(context, true)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, WidgetUpdateReceiver::class.java),
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