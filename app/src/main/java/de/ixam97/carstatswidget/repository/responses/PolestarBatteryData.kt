package de.ixam97.carstatswidget.repository.responses

data class PolestarBatteryData(
    val `data`: Data
) {
    data class Data(
        val getBatteryData: GetBatteryData
    ) {
        data class GetBatteryData(
            val batteryChargeLevelPercentage: Int,
            val chargerConnectionStatus: String,
            val chargingStatus: String,
            val estimatedDistanceToEmptyKm: Int,
            val eventUpdatedTimestamp: EventUpdatedTimestamp
        ) {
            data class EventUpdatedTimestamp(
                val iso: String
            )
        }
    }
}