package com.docesforg.bura.condition

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class ConditionMoment(
    hour: LocalDateTime,
    val condition: Condition
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $condition"
}