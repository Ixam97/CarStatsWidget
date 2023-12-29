package de.ixam97.carstatswidget.util

import android.content.Context
import de.ixam97.carstatswidget.R

sealed interface AvailableApis {
    object Tibber: AvailableApis
    object Polestar: AvailableApis

    companion object {
        fun get(name: String?): AvailableApis {
            return when (name) {
                "Tibber" -> Tibber
                "Polestar" -> Polestar
                else -> Tibber
            }
        }

        val list = listOf(
            Tibber, Polestar
        )

        fun loginHints(api: AvailableApis): Int? = when (api) {
            Polestar -> R.string.api_hint_polestar
            else -> null
        }
    }
}