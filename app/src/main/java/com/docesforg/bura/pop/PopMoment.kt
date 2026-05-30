package com.docesforg.bura.pop

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class PopMoment(
    hour: LocalDateTime,
    val pop: Pop
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $pop"
}