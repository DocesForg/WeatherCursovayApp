package com.docesforg.bura.place.saved

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.place.Place
import com.docesforg.bura.temperature.Temperature
import java.time.LocalTime

data class SavedPlace(
    val place: Place,
    val time: LocalTime,
    val selected: Boolean,
    val conditions: Conditions?
) {
    data class Conditions(
        val temp: Temperature,
        val minTemp: Temperature,
        val maxTemp: Temperature,
        val condition: Condition
    )
}