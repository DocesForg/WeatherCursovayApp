package com.docesforg.bura.summary.hourly

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.PopPeriod
import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.sun.SunPeriod
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperaturePeriod
import java.time.LocalDateTime

fun getHourlySummary(
    now: LocalDateTime,
    tempPeriod: TemperaturePeriod,
    popPeriod: PopPeriod,
    condPeriod: ConditionPeriod,
    sunPeriod: SunPeriod?
): ForecastResult<List<HourSummary>> {
    val futureTemps = tempPeriod.momentsFrom(now, takeMoments = 24) ?: return ForecastResult.Outdated
    val futurePops = popPeriod.momentsFrom(now, takeMoments = 24) ?: return ForecastResult.Outdated
    val futureDesc = condPeriod.momentsFrom(now, takeMoments = 24) ?: return ForecastResult.Outdated
    val combinedWeatherData = buildList {
        for (i in futureTemps.indices) {
            add(
                HourSummary.Weather(
                    time = futureTemps[i].hour,
                    isNow = i == 0,
                    temp = futureTemps[i].temperature,
                    pop = futurePops[i].pop.takeIf { it.value > 0 },
                    desc = futureDesc[i].condition
                )
            )
        }
    }
    val combinedSunData = sunPeriod
        ?.momentsFrom(now, takeMomentsUpToHoursInFuture = 24)
        ?.map {
            HourSummary.Sun(
                time = it.time,
                event = it.event
            )
        }
        ?: listOf()

    return ForecastResult.Success((combinedWeatherData + combinedSunData).sortedBy { it.time })
}

sealed interface HourSummary {
    val time: LocalDateTime

    data class Weather(
        override val time: LocalDateTime,
        val isNow: Boolean,
        val temp: Temperature,
        val pop: Pop?,
        val desc: Condition
    ) : HourSummary

    data class Sun(
        override val time: LocalDateTime,
        val event: SunEvent
    ) : HourSummary
}