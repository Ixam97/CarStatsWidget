package de.ixam97.carstatswidget.util

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb


fun adjustColorValue(color: androidx.compose.ui.graphics.Color, value: Float): androidx.compose.ui.graphics.Color {
    val hsv = FloatArray(3)
    Color.colorToHSV(color.toArgb(), hsv)

    hsv[2] = value

    val newColor = Color.HSVToColor(hsv)

    return androidx.compose.ui.graphics.Color(newColor)
}
