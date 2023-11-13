package de.ixam97.carstatswidget.repository.tibberData

data class ElectricVehicle(
    val battery: Battery,
    val imgUrl: String,
    val lastSeen: String,
    val name: String,
    val shortName: String,
    val id: String
)