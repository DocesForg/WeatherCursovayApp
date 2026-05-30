package com.docesforg.bura.visibility

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class VisibilityMoment(
    hour: LocalDateTime,
    val visibility: Visibility,
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $visibility"
}