package com.docesforg.bura.pressure

import com.docesforg.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class PressurePeriod(moments: List<PressureMoment>) : HourPeriod<PressureMoment>(moments) {
    val minimum get() = minOf { it.pressure }

    val average get() = map { it.pressure }.reduce { acc, pressure -> acc + pressure } / size

    override fun momentsUntil(hourExclusive: LocalDateTime, takeMoments: Int?): PressurePeriod? =
        super.momentsUntil(hourExclusive, takeMoments)?.let { PressurePeriod(it) }

    override fun getDay(day: LocalDate): PressurePeriod? =
        super.getDay(day)?.let { PressurePeriod(it) }
}