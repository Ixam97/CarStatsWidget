package de.ixam97.carstatswidget.repository

import de.ixam97.carstatswidget.repository.tibberBarerToken.TibberBarerToken
import de.ixam97.carstatswidget.repository.tibberCredentials.TibberCredentials
import de.ixam97.carstatswidget.repository.tibberData.TibberData
import de.ixam97.carstatswidget.repository.tibberQuery.TibberQuery
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface TibberApi {

    @Headers("Content-Type: application/json")
    @POST("/login.credentials")
    suspend fun authenticateTibber(@Body tibberCredentials: TibberCredentials) : Response<TibberBarerToken>

    @Headers("Content-Type: application/json")
    @POST("/v4/gql")
    suspend fun fetchTibberData(
        @Header("Authorization") auth: String,
        @Body query: TibberQuery
    ) : Response<TibberData>
}