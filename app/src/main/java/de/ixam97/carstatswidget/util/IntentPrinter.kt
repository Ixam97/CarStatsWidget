package de.ixam97.carstatswidget.util

import android.content.Intent


fun intentToString(intent: Intent?): String {
    if (intent == null) return ""
    val stringBuilder = StringBuilder("action: ")
        .append(intent.action)
        .append(" data: ")
        .append(intent.dataString)
        .append(" extras: ")
    for (key in intent.extras!!.keySet()) stringBuilder.append(key).append("=").append(
        intent.extras!![key]
    ).append(" ")
    return stringBuilder.toString()
}