package com.docesforg.bura.temperature

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class TemperatureMoment(
    hour: LocalDateTime,
    val temperature: Temperature
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $temperature"
}
