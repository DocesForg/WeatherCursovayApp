package com.docesforg.bura.place

import java.time.ZoneId

data class Location(
    val timeZone: ZoneId,
    val coordinates: Coordinates
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    val id: String get() = "$latitude;$longitude"

    companion object {
        fun fromId(id: String): Coordinates {
            val (lat, lon) = id.split(";").map { it.toDouble() }
            return Coordinates(latitude = lat, longitude = lon)
        }
    }
}