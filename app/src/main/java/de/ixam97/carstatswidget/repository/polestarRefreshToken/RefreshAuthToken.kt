package de.ixam97.carstatswidget.repository.polestarRefreshToken

data class RefreshAuthToken(
    val access_token: String,
    val expires_in: Int,
    val id_token: Any,
    val refresh_token: String
)