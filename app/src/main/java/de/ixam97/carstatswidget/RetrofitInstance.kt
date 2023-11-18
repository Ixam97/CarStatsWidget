package de.ixam97.carstatswidget

import de.ixam97.carstatswidget.repository.GitHubVersionChecker
import de.ixam97.carstatswidget.repository.TibberApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val tibberApi: TibberApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://app.tibber.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TibberApi::class.java)
    }
    val gitHubVersionChecker: GitHubVersionChecker by lazy {
        Retrofit.Builder()
            .baseUrl("https://github.com")
            .build()
            .create(GitHubVersionChecker::class.java)
    }
}