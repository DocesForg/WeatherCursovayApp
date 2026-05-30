package com.docesforg.bura.summary.daily

import com.docesforg.bura.pop.Pop
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.pop.PopPeriod
import com.docesforg.bura.temperature.TemperaturePeriod
import java.time.LocalDate
import java.time.LocalDateTime

fun getDailySummary(
    now: LocalDateTime,
    tempPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod,
    popPeriod: PopPeriod
): ForecastResult<DailySummary> {
    val nowDate = now.toLocalDate()
    val futureTempDays = tempPeriod.daysFrom(nowDate) ?: return ForecastResult.Outdated
    val popDays = popPeriod.momentsFrom(now)?.daysFrom(nowDate) ?: return ForecastResult.Outdated
    val descDays = condPeriod.momentsFrom(now)?.daysFrom(nowDate) ?: return ForecastResult.Outdated
    return ForecastResult.Success(
        DailySummary(
            minTemp = futureTempDays.minOf { it.minimum },
            maxTemp = futureTempDays.maxOf { it.maximum },
            days = buildList {
                for (i in futureTempDays.indices) {
                    add(
                        DaySummary(
                            isToday = i == 0,
                            time = futureTempDays[i].first().hour.toLocalDate(),
                            tempNow = futureTempDays[i][now]?.temperature,
                            min = futureTempDays[i].minimum,
                            max = futureTempDays[i].maximum,
                            pop = popDays[i].maximum.takeIf { it.value > 0 },
                            desc = descDays[i].day ?: descDays[i].night!!
                        )
                    )
                }
            }
        ),
    )
}

data class DailySummary(
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val days: List<DaySummary>
)

data class DaySummary(
    val isToday: Boolean,
    val time: LocalDate,
    val tempNow: Temperature?,
    val min: Temperature,
    val max: Temperature,
    val pop: Pop?,
    val desc: Condition
)