package de.ixam97.carstatswidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.ixam97.carstatswidget.repository.CarDataWorker

class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetUpdateReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        CarDataWorker.enqueue(context, true)
    }

}