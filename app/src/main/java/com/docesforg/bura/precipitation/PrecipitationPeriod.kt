package com.docesforg.bura.precipitation

import com.docesforg.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class PrecipitationPeriod(moments: List<PrecipitationMoment>) : HourPeriod<PrecipitationMoment>(moments) {
    val total: MixedPrecipitation get() = map { it.precipitation }.reduce { acc, precipitation -> acc + precipitation }
    val max: MixedPrecipitation get() = maxOf { it.precipitation }

    override fun momentsUntil(hourExclusive: LocalDateTime, takeMoments: Int?) =
        super.momentsUntil(hourExclusive, takeMoments)?.let { PrecipitationPeriod(it) }

    override fun momentsFrom(hourInclusive: LocalDateTime, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { PrecipitationPeriod(it) }

    override fun daysFrom(dayInclusive: LocalDate, takeDays: Int?) =
        super.daysFrom(dayInclusive, takeDays)?.map { PrecipitationPeriod(it) }
}