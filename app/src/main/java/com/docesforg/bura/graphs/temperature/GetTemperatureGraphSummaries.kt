package com.docesforg.bura.graphs.temperature

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.temperature.TemperaturePeriod
import java.time.LocalDate
import java.time.LocalDateTime

fun getTemperatureGraphSummaries(
    now: LocalDateTime,
    tempPeriod: TemperaturePeriod,
    feelsPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod
): ForecastResult<List<TemperatureGraphSummary>> {
    val tempDays = tempPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    val conditionDays = condPeriod.momentsFrom(now)?.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    val feelsLikeNow = feelsPeriod[now]?.temperature ?: return ForecastResult.Outdated

    return ForecastResult.Success(
        data = tempDays.mapIndexed { idx, tempDay ->
            val day = tempDay.first().hour.toLocalDate()
            val minTemp = tempDay.minimum
            val maxTemp = tempDay.maximum
            val conditionDay = conditionDays[idx]
            val condition = conditionDay[now]?.condition ?: conditionDay.day ?: conditionDay.night!!
            val nowTemp = tempDay[now]?.temperature

            TemperatureGraphSummary(
                day = day,
                minTemp = minTemp,
                maxTemp = maxTemp,
                condition = condition,
                now = nowTemp?.let {
                    TemperatureGraphNowSummary(
                        temp = nowTemp,
                        feelsLike = feelsLikeNow
                    )
                }
            )
        }
    )
}

data class TemperatureGraphSummary(
    val day: LocalDate,
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val condition: Condition,
    val now: TemperatureGraphNowSummary?
)

data class TemperatureGraphNowSummary(
    val temp: Temperature,
    val feelsLike: Temperature
)