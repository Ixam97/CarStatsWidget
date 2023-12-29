package de.ixam97.carstatswidget.repository

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
sealed interface PolestarDataStatus {
    @Serializable
    object NotAuthenticated: PolestarDataStatus

    @Serializable
    object Available: PolestarDataStatus

    @Serializable
    object Unavailable: PolestarDataStatus

    @Serializable
    object Loading: PolestarDataStatus
}

@Serializable
data class PolestarData(
    val status: PolestarDataStatus = PolestarDataStatus.Unavailable,
    val batteryChargeLevelPercentage: Int? = null,
    val chargerConnectionStatus: Int? = null,
    val chargingStatus: Int? = null,
) {}

data class PolestarToken(
    val accessToken: String,
    val refreshToken: String,
    val expirationTime: Date
)