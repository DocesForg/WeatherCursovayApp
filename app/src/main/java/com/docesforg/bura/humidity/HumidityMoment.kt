package com.docesforg.bura.humidity

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class HumidityMoment(
    hour: LocalDateTime,
    val humidity: Humidity,
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $humidity"
}