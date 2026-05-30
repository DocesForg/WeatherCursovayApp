package com.docesforg.bura.temperature

import com.docesforg.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class TemperaturePeriod(moments: List<TemperatureMoment>) : HourPeriod<TemperatureMoment>(moments) {
    val minimum get() = minOf { it.temperature }

    val maximum get() = maxOf { it.temperature }

    override fun getDay(day: LocalDate) =
        super.getDay(day)?.let { TemperaturePeriod(it) }

    override fun momentsFrom(hourInclusive: LocalDateTime, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { TemperaturePeriod(it) }

    override fun daysFrom(dayInclusive: LocalDate, takeDays: Int?) =
        super.daysFrom(dayInclusive, takeDays)?.map { TemperaturePeriod(it) }
}
