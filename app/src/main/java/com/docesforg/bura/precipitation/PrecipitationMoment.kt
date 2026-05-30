package com.docesforg.bura.precipitation

import com.docesforg.bura.forecast.HourMoment
import java.time.LocalDateTime

class PrecipitationMoment(
    hour: LocalDateTime,
    val precipitation: MixedPrecipitation
) : HourMoment(hour) {
    override fun toString(): String = "$hour: $precipitation"
}