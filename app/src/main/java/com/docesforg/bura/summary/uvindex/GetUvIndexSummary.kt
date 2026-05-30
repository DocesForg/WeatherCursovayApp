package com.docesforg.bura.summary.uvindex

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.uvindex.UvIndex
import com.docesforg.bura.uvindex.UvIndexPeriod
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

fun getUvIndexSummary(
    now: LocalDateTime,
    uvIndexPeriod: UvIndexPeriod
): ForecastResult<UvIndexSummary> {
    val futureUv = uvIndexPeriod.getDay(now.toLocalDate())?.momentsFrom(now) ?: return ForecastResult.Outdated
    val protection = futureUv.protectionWindows.firstOrNull()?.let {
        if (it.startInclusive == now.truncatedTo(ChronoUnit.HOURS)) {
            if (it.endExclusive == null) {
                UseProtection.UntilEndOfDay
            } else {
                UseProtection.Until(
                    endExclusive = it.endExclusive.toLocalTime()
                )
            }
        } else {
            if (it.endExclusive == null) {
                UseProtection.FromUntilEndOfDay(
                    startInclusive = it.startInclusive.toLocalTime()
                )
            } else {
                UseProtection.FromUntil(
                    startInclusive = it.startInclusive.toLocalTime(),
                    endExclusive = it.endExclusive.toLocalTime()
                )
            }
        }
    } ?: UseProtection.None
    return ForecastResult.Success(
        UvIndexSummary(
            now = uvIndexPeriod[now]?.uvIndex ?: return ForecastResult.Outdated,
            useProtection = protection
        )
    )
}

data class UvIndexSummary(
    val now: UvIndex,
    val useProtection: UseProtection
)

sealed interface UseProtection {
    data class FromUntil(
        val startInclusive: LocalTime,
        val endExclusive: LocalTime
    ) : UseProtection

    data class Until(val endExclusive: LocalTime) : UseProtection

    data object UntilEndOfDay : UseProtection

    data class FromUntilEndOfDay(val startInclusive: LocalTime) : UseProtection

    data object None : UseProtection
}