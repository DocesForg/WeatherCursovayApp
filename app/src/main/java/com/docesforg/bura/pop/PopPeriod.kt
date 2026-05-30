package com.docesforg.bura.pop

import com.docesforg.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class PopPeriod(moments: List<PopMoment>) : HourPeriod<PopMoment>(moments) {
    val maximum get() = maxOf { it.pop }
    val once: Pop get() {
        val firstPop = first().pop
        if (size == 1) return firstPop

        var probNone = 1 - (firstPop.value / 100)
        for (i in 1..lastIndex) {
            probNone *= 1 - (get(i).pop.value / 100)
        }
        val probOnce = 1 - probNone
        return Pop(value = probOnce * 100)
    }

    override fun momentsFrom(hourInclusive: LocalDateTime, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { PopPeriod(it) }

    override fun daysFrom(dayInclusive: LocalDate, takeDays: Int?) =
        super.daysFrom(dayInclusive, takeDays)?.map { PopPeriod(it) }
}