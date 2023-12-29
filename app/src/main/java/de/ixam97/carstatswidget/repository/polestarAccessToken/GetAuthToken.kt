package de.ixam97.carstatswidget.repository.polestarAccessToken

data class GetAuthToken(
    val access_token: String,
    val expires_in: Int,
    val id_token: String,
    val refresh_token: String
)