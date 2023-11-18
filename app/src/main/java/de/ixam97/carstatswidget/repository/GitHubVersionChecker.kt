package de.ixam97.carstatswidget.repository

import retrofit2.Response
import retrofit2.http.GET

interface GitHubVersionChecker {

    @GET("/Ixam97/CarStatsWidget/releases/latest/")
    suspend fun fetchGitHubVersion(): Response<Void>
}