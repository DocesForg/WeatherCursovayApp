package com.docesforg.bura.summary.feelslike

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperaturePeriod
import java.time.LocalDateTime
import kotlin.math.absoluteValue

fun getFeelsLikeSummary(
    now: LocalDateTime,
    tempPeriod: TemperaturePeriod,
    feelsPeriod: TemperaturePeriod
): ForecastResult<FeelsLikeSummary> {
    val feelsNow = feelsPeriod[now]?.temperature ?: return ForecastResult.Outdated
    val actualNow = tempPeriod[now]?.temperature ?: return ForecastResult.Outdated
    return ForecastResult.Success(
        FeelsLikeSummary(
            feelsLikeNow = feelsNow,
            actualNow = actualNow,
            vsActual = calculateComparedToActual(
                actualTemp = actualNow,
                feelsLikeTemp = feelsNow
            )
        ),
    )
}

private fun calculateComparedToActual(
    actualTemp: Temperature,
    feelsLikeTemp: Temperature
): FeelsVsActual {
    val actualCelsius = actualTemp.convertTo(Temperature.Unit.DegreesCelsius).value
    val feelsCelsius = feelsLikeTemp.convertTo(Temperature.Unit.DegreesCelsius).value
    return when {
        (feelsCelsius - actualCelsius).absoluteValue < 1 -> FeelsVsActual.Similar
        feelsCelsius <= 10 -> if (feelsCelsius < actualCelsius) FeelsVsActual.Colder else FeelsVsActual.Warmer
        feelsCelsius <= 25 -> if (feelsCelsius < actualCelsius) FeelsVsActual.Cooler else FeelsVsActual.Warmer
        else -> if (feelsCelsius < actualCelsius) FeelsVsActual.Cooler else FeelsVsActual.Hotter
    }
}

data class FeelsLikeSummary(
    val feelsLikeNow: Temperature,
    val actualNow: Temperature,
    val vsActual: FeelsVsActual
)

enum class FeelsVsActual {
    Colder,
    Cooler,
    Similar,
    Warmer,
    Hotter
}