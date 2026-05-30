package com.docesforg.bura.condition

import com.docesforg.bura.forecast.HourPeriod
import java.time.LocalDate
import java.time.LocalDateTime

class ConditionPeriod(
    moments: List<ConditionMoment>
) : HourPeriod<ConditionMoment>(moments) {
    val day get() = representative(isDay = true)

    val night get() = representative(isDay = false)

    override fun momentsFrom(hourInclusive: LocalDateTime, takeMoments: Int?) =
        super.momentsFrom(hourInclusive, takeMoments)?.let { ConditionPeriod(it) }

    override fun daysFrom(dayInclusive: LocalDate, takeDays: Int?) =
        super.daysFrom(dayInclusive, takeDays)?.map { ConditionPeriod(it) }

    override fun getDay(day: LocalDate) =
        super.getDay(day)?.let { ConditionPeriod(it) }

    private fun representative(isDay: Boolean): Condition? {
        val groupedByCode = filter { it.condition.isDay == isDay }
            .groupBy { it.condition }
            .takeIf { it.isNotEmpty() }
            ?: return null
        val hasSevereConditions = groupedByCode.any { it.key.wmoCode >= 50 }
        val allTheSame = groupedByCode.map { it.value.size }.toSet().size == 1

        return if (hasSevereConditions || allTheSame) {
            groupedByCode.maxBy { it.key.wmoCode }.key
        } else {
            groupedByCode.maxBy { it.value.size }.key
        }
    }
}