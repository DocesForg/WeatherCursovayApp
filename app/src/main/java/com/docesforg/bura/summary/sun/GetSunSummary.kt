package com.docesforg.bura.summary.sun

import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.sun.SunMoment
import com.docesforg.bura.sun.SunPeriod
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private const val LATER_HOUR_THRESH = 25

fun getSunSummary(
    now: LocalDateTime,
    sunPeriod: SunPeriod?,
    condPeriod: ConditionPeriod
): ForecastResult<SunSummary> {
    val futureSun = sunPeriod?.momentsFrom(now)
    val firstSun = futureSun?.firstOrNull()
    return when {
        firstSun == null -> outOfSight(now, condPeriod)
        firstSun.event == SunEvent.Sunrise -> ForecastResult.Success(sunrise(now, futureSun, firstSun))
        else -> ForecastResult.Success(sunset(now, futureSun, firstSun))
    }
}

private fun outOfSight(
    now: LocalDateTime,
    condPeriod: ConditionPeriod
): ForecastResult<SunSummary> {
    val futureDesc = condPeriod.momentsFrom(now) ?: return ForecastResult.Outdated
    val isDayNow = futureDesc[now]!!.condition.isDay
    val lastMoment = futureDesc.last().hour
    val duration = Duration.between(now, lastMoment).plusHours(1)
    return ForecastResult.Success(
        if (isDayNow) Sunset.OutOfSight(duration)
        else Sunrise.OutOfSight(duration)
    )
}

private fun sunrise(
    now: LocalDateTime,
    futureSun: List<SunMoment>,
    firstSun: SunMoment
): Sunrise {
    val sunrise = firstSun.time
    return if (ChronoUnit.HOURS.between(now, sunrise) >= LATER_HOUR_THRESH) {
        Sunrise.Later(sunrise)
    } else {
        val sunset = futureSun[1].time
        if (ChronoUnit.HOURS.between(now, sunset) < LATER_HOUR_THRESH) {
            Sunrise.WithSunsetSoon(
                time = sunrise.toLocalTime(),
                sunset = futureSun[1].time.toLocalTime()
            )
        } else {
            Sunrise.WithSunsetLater(
                time = sunrise.toLocalTime(),
                sunset = futureSun[1].time
            )
        }
    }
}

private fun sunset(
    now: LocalDateTime,
    futureSun: List<SunMoment>,
    firstSun: SunMoment
): Sunset {
    val sunset = firstSun.time
    return if (ChronoUnit.HOURS.between(now, sunset) >= LATER_HOUR_THRESH) {
        Sunset.Later(sunset)
    } else {
        val sunrise = futureSun[1].time
        if (ChronoUnit.HOURS.between(now, sunrise) < LATER_HOUR_THRESH) {
            Sunset.WithSunriseSoon(
                time = firstSun.time.toLocalTime(),
                sunrise = futureSun[1].time.toLocalTime()
            )
        } else {
            Sunset.WithSunriseLater(
                time = firstSun.time.toLocalTime(),
                sunrise = futureSun[1].time
            )
        }
    }
}

sealed interface SunSummary

sealed interface Sunrise : SunSummary {
    data class WithSunsetSoon(
        val time: LocalTime,
        val sunset: LocalTime
    ) : Sunrise

    data class WithSunsetLater(
        val time: LocalTime,
        val sunset: LocalDateTime
    ) : Sunrise

    data class Later(val time: LocalDateTime) : Sunrise

    data class OutOfSight(val forDuration: Duration) : Sunrise
}

sealed interface Sunset : SunSummary {
    data class WithSunriseSoon(
        val time: LocalTime,
        val sunrise: LocalTime
    ) : Sunset

    data class WithSunriseLater(
        val time: LocalTime,
        val sunrise: LocalDateTime
    ) : Sunset

    data class Later(val time: LocalDateTime) : Sunset

    data class OutOfSight(val forDuration: Duration) : Sunset
}