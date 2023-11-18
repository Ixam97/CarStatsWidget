package de.ixam97.carstatswidget.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsPalette(
    val greenSuccess: Color = Color.Unspecified,
)

val LightGreenSuccess = Color(color = 0xFF257018)

val DarkGreenSuccess = Color(color = 0xFF7BCC6C)

val LightCustomColorsPalette = CustomColorsPalette(
    greenSuccess = LightGreenSuccess
)

val DarkCustomColorsPalette = CustomColorsPalette(
    greenSuccess = DarkGreenSuccess
)

val LocalCustomColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }