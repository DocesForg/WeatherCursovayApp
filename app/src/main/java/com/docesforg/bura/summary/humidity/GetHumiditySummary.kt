package com.docesforg.bura.summary.humidity

import com.docesforg.bura.humidity.Humidity
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.humidity.HumidityPeriod
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperaturePeriod
import java.time.LocalDateTime

fun getHumiditySummary(
    now: LocalDateTime,
    humidityPeriod: HumidityPeriod,
    dewPointPeriod: TemperaturePeriod
): ForecastResult<HumiditySummary> {
    return ForecastResult.Success(
        HumiditySummary(
            humidityNow = humidityPeriod[now]?.humidity ?: return ForecastResult.Outdated,
            dewPointNow = dewPointPeriod[now]?.temperature ?: return ForecastResult.Outdated
        ),
    )
}

data class HumiditySummary(
    val humidityNow: Humidity,
    val dewPointNow: Temperature
)