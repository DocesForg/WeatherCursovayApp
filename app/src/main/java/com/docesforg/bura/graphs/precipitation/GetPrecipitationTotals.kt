package com.docesforg.bura.graphs.precipitation

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.PrecipitationPeriod
import java.time.LocalDate
import java.time.LocalDateTime

private const val PAST_HOURS = 24
private const val FUTURE_HOURS = 24

fun getPrecipitationTotals(
    now: LocalDateTime,
    precipPeriod: PrecipitationPeriod
): ForecastResult<List<PrecipitationTotal>> {
    val today = getToday(precipPeriod, now) ?: return ForecastResult.Outdated
    val days = precipPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    val daysAfterToday = days.subList(1, days.size)
    return ForecastResult.Success(
        data = buildList {
            add(today)
            addAll(
                daysAfterToday.map { day ->
                    PrecipitationTotal.OtherDay(
                        day = day.first().hour.toLocalDate(),
                        total = day.total.reduce()
                    )
                }
            )
        }
    )
}

private fun getToday(period: PrecipitationPeriod, now: LocalDateTime): PrecipitationTotal.Today? {
    val past = period.momentsUntil(now, takeMoments = PAST_HOURS) ?: return null
    val future = period.momentsFrom(now, takeMoments = FUTURE_HOURS) ?: return null
    return PrecipitationTotal.Today(
        day = now.toLocalDate(),
        past = TotalPrecipitationInHours(
            hours = past.size,
            total = past.total.reduce()
        ),
        future = TotalPrecipitationInHours(
            hours = future.size,
            total = future.total.reduce()
        )
    )
}

sealed interface PrecipitationTotal {
    val day: LocalDate

    data class Today(
        override val day: LocalDate,
        val past: TotalPrecipitationInHours,
        val future: TotalPrecipitationInHours
    ) : PrecipitationTotal

    data class OtherDay(
        override val day: LocalDate,
        val total: Precipitation
    ) : PrecipitationTotal
}

data class TotalPrecipitationInHours(
    val hours: Int,
    val total: Precipitation
)