package com.docesforg.bura.summary.pressure

import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.pressure.PressurePeriod
import java.time.LocalDateTime
import kotlin.math.absoluteValue

fun getPressureSummary(
    now: LocalDateTime,
    pressurePeriod: PressurePeriod
): ForecastResult<PressureSummary> {
    val pressureToday = pressurePeriod.getDay(now.toLocalDate()) ?: return ForecastResult.Outdated
    val pressureNow = pressurePeriod[now]?.pressure ?: return ForecastResult.Outdated

    val nowHpa = pressureNow.convertTo(Pressure.Unit.Hectopascal).value
    val pastHpa = pressurePeriod.momentsUntil(now, takeMoments = 2)?.firstOrNull()
        ?.pressure?.convertTo(Pressure.Unit.Hectopascal)?.value
        ?: return ForecastResult.Outdated
    val diffHpa = (nowHpa - pastHpa).absoluteValue
    val trend = when {
        diffHpa < 1 -> PressureTrend.Stable
        diffHpa > 0 -> PressureTrend.Rising
        else -> PressureTrend.Falling
    }

    return ForecastResult.Success(
        PressureSummary(
            now = pressureNow,
            average = pressureToday.average,
            trend = trend
        ),
    )
}

data class PressureSummary(
    val now: Pressure,
    val average: Pressure,
    val trend: PressureTrend
)

enum class PressureTrend {
    Rising, Falling, Stable
}