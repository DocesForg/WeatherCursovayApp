package com.docesforg.bura.uvindex

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class UvIndexMoment(
    hour: LocalDateTime,
    val uvIndex: UvIndex
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $uvIndex"
}