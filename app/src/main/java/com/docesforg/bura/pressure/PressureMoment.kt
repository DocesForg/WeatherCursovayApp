package com.docesforg.bura.pressure

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class PressureMoment(
    hour: LocalDateTime,
    val pressure: Pressure
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $pressure"
}