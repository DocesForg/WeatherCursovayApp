package com.docesforg.bura.wind

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class WindMoment(
    hour: LocalDateTime,
    val wind: Wind
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $wind"
}