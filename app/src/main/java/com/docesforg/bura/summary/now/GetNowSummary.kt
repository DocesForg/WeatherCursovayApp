package com.docesforg.bura.summary.now

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.temperature.TemperaturePeriod
import java.time.LocalDateTime

fun getNowSummary(
    now: LocalDateTime,
    tempPeriod: TemperaturePeriod,
    feelsPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod
): ForecastResult<NowSummary> {
    val tempToday = tempPeriod.getDay(now.toLocalDate()) ?: return ForecastResult.Outdated
    return ForecastResult.Success(
        NowSummary(
            temp = tempPeriod[now]?.temperature ?: return ForecastResult.Outdated,
            feelsLike = feelsPeriod[now]?.temperature ?: return ForecastResult.Outdated,
            minTemp = tempToday.minimum,
            maxTemp = tempToday.maximum,
            cond = condPeriod[now]?.condition ?: return ForecastResult.Outdated
        ),
    )
}

data class NowSummary(
    val temp: Temperature,
    val feelsLike: Temperature,
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val cond: Condition
)