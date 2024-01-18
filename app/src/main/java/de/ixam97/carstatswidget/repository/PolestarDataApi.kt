package de.ixam97.carstatswidget.repository

import de.ixam97.carstatswidget.repository.polestarAccessToken.PolestarAccessToken
import de.ixam97.carstatswidget.repository.polestarRefreshToken.PolestarRefreshToken
import de.ixam97.carstatswidget.repository.polestarTokenValidity.PolestarTokenValidity
import de.ixam97.carstatswidget.repository.responses.PolestarBatteryData
import de.ixam97.carstatswidget.repository.responses.PolestarGetCars
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface PolestarDataApi {

    @Headers("content-type: application/json")
    @POST("/eu-north-1/auth/")//?query=query%20getAuthToken(%24code%3A%20String!)%20%7B%0A%20%20getAuthToken(code%3A%20%24code)%20%7B%0A%20%20%20%20id_token%0A%20%20%20%20access_token%0A%20%20%20%20refresh_token%0A%20%20%20%20expires_in%0A%20%20%7D%0A%7D%0A&operationName=getAuthToken")
    suspend fun getBearerToken(
        // @Query("query", encoded = false) query: String = "query getAuthToken(\$code: String!) {getAuthToken(code: \$code) {id_token access_token refresh_token expires_in}}",
        // @Query("operationName", encoded = false) operationName: String = "getAuthToken",
        // @Query("variables", encoded = true) variables: String
        @Body body: PolestarRepository.GetBearerTokenBody
    ): Response<PolestarAccessToken>

    // data class GetBearerTokenBody(
    //     val query: String = "query getAuthToken(\$code: String!) {getAuthToken(code: \$code) {id_token access_token refresh_token expires_in}}",
    //     val operationName: String = "getAuthToken",
    //     val variables: String
    // )

    @Headers("content-type: application/json")
    @POST("/eu-north-1/auth/")
    suspend fun refreshToken(
        // @Query("query", encoded = false) query: String = "query refreshAuthToken(\$token: String!) {refreshAuthToken(token: \$token) {access_token expires_in id_token refresh_token}}",
        // @Query("query", encoded = true) query: String = "query%20refreshAuthToken(%24token%3A%20String!)%20%7BrefreshAuthToken(token%3A%20%24token)%20%7Baccess_token%20expires_in%20id_token%20refresh_token%7D%7D",
        // @Query("variables", encoded = false) variables: String,
        @Header("Authorization") auth: String,
        @Body body: PolestarRepository.RefreshTokenBody
    ): Response<PolestarRefreshToken>


    @Headers("content-type: application/json")
    @POST("/eu-north-1/my-star/")
    suspend fun checkTokenValidity(
        // @Query("query", encoded = false) query: String = "query introspectToken(\$token: String!) {introspectToken(token: \$token) {active}}",
        // @Query("operationName", encoded = false) operationName: String = "introspectToken",
        // @Query("variables", encoded = false) variables: String,
        @Header("Authorization") auth: String,
        @Body body: PolestarRepository.ValidateTokenBody
    ): Response<PolestarTokenValidity>

    @Headers("content-type: application/json")
    @POST("/eu-north-1/my-star/")
    suspend fun getCars(
        @Header("Authorization") auth: String,
        @Body body: PolestarRepository.GetCarsBody = PolestarRepository.GetCarsBody()
    ): Response<PolestarGetCars>

    @Headers("content-type: application/json")
    @POST("/eu-north-1/mystar-v2/")
    suspend fun getBatteryData(
        @Header("Authorization") auth: String,
        @Body body: PolestarRepository.GetBatteryDataBody
    ): Response<PolestarBatteryData>
}