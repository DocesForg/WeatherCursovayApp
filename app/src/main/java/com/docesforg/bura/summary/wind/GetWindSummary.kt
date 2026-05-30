package com.docesforg.bura.summary.wind

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.gust.GustPeriod
import com.docesforg.bura.wind.Wind
import com.docesforg.bura.wind.WindPeriod
import com.docesforg.bura.wind.WindSpeed
import java.time.LocalDateTime

fun getWindSummary(
    now: LocalDateTime,
    windPeriod: WindPeriod,
    gustPeriod: GustPeriod
): ForecastResult<WindSummary> {
    return ForecastResult.Success(
        WindSummary(
            windNow = windPeriod[now]?.wind ?: return ForecastResult.Outdated,
            gustNow = gustPeriod[now]?.speed ?: return ForecastResult.Outdated
        )
    )
}

data class WindSummary(
    val windNow: Wind,
    val gustNow: WindSpeed
)