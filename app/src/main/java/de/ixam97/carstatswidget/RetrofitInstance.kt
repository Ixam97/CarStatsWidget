package de.ixam97.carstatswidget

import de.ixam97.carstatswidget.repository.GitHubVersionChecker
import de.ixam97.carstatswidget.repository.PolestarAuthApi
import de.ixam97.carstatswidget.repository.PolestarDataApi
import de.ixam97.carstatswidget.repository.TibberApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val noRedirectClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

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
    val polestarAuthApi: PolestarAuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://polestarid.eu.polestar.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(noRedirectClient)
            .build()
            .create(PolestarAuthApi::class.java)

    }
    val polestarDataApi: PolestarDataApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://pc-api.polestar.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PolestarDataApi::class.java)
    }
}