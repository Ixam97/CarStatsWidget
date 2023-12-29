package de.ixam97.carstatswidget.repository.responses

data class TibberData(
    val `data`: Data
)

data class Data(
    val me: Me
)

data class Me(
    val homes: List<Home>
)

data class Home(
    val electricVehicles: List<ElectricVehicle>
)

data class ElectricVehicle(
    val battery: Battery,
    val imgUrl: String,
    val lastSeen: String,
    val name: String,
    val shortName: String,
    val id: String
)

data class Battery(
    val isCharging: Boolean,
    val percent: Int
)