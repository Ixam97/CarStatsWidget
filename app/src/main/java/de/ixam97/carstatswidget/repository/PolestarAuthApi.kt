package de.ixam97.carstatswidget.repository

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PolestarAuthApi {

    /**
     * Der Response-Body dieses Request ist uninteressant. Wichtig sind die Header:
     *  - Location: Die URL zur Polestar-Anmeldemaske, die normalerweise Ziel des Redirect ist.
     *              Diese enthalt den URL-Parameter 'resumePath', welcher extrahiert werden muss
     *              und im nächsten Request das 'path_token' darstellt.
     *  - Set-Cookie: Enhält mehrere Cookies, wovon einer für den nächsten Request gebraucht wird
     *                Beispiel: PF=QQkRdSF7t8Ts1MHWWzcH4Z. Den einfach als String zwischenspeichern
     *                und im nächsten Request als 'cookie' im den Header angeben
     *                Die anderen Cookies können ignoriert werden.
     */
    @GET("/as/authorization.oauth2?response_type=code&client_id=polmystar&redirect_uri=https%3A%2F%2Fwww.polestar.com%2Fsign-in-callback&scope=openid%20profile%20email%20customer:attributes")
    suspend fun getLoginFlowTokens(): Response<Void>

    /**
     * Wie beim ersten Request ist der Body uninteressant und die Header von interesse:
     *  - Location: Enthalt eine URL mit dem Parameter 'code'. Mit diesem kann das Bearer-Token
     *              angefragt werden.
     * Alle weiteren API-Calls funktionieren, wie man es von REST-APIs gewohnt ist.
     */
    @FormUrlEncoded
    @POST("/as/{path_token}/resume/as/authorization.ping?client_id=polmystar")
    suspend fun performLogin(
        @Path(value = "path_token", encoded = true) pathToken: String,
        @Field("pf.username") email: String,
        @Field("pf.pass") password: String,
        @Header("cookie") cookie: String
        ): Response<Void>

}