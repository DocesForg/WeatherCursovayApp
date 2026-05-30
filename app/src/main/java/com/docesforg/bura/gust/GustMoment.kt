package com.docesforg.bura.gust

import com.docesforg.bura.forecast.HourMoment
import com.docesforg.bura.wind.WindSpeed
import java.time.LocalDateTime

class GustMoment(
    hour: LocalDateTime,
    val speed: WindSpeed
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $speed"
}