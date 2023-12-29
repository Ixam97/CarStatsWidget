package de.ixam97.carstatswidget.repository.tibberQuery

data class TibberQuery(
    val query: String = "{me {homes {electricVehicles {id lastSeen name shortName battery {percent isCharging} imgUrl}}}}"
)
