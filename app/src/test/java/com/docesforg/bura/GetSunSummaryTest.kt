package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.summary.sun.Sunrise
import com.docesforg.bura.summary.sun.Sunset
import com.docesforg.bura.summary.sun.getSunSummary
import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.sun.SunMoment
import com.docesforg.bura.sun.SunPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.temporal.ChronoUnit

class GetSunSummaryTest {
    @Test
    fun `sunrise and sunset soon`() = runTest {
        val now = unixEpochStart
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.HOURS)
        val sunPeriod = SunPeriod(
            listOf(
                SunMoment(firstMoment, event = SunEvent.Sunrise),
                SunMoment(secondMoment, event = SunEvent.Sunset)
            )
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, condition = Condition(1, true))
            )
        )

        val summary = getSunSummary(now, sunPeriod, condPeriod)
        assertEquals(
            Sunrise.WithSunsetSoon(
                time = firstMoment.toLocalTime(),
                sunset = secondMoment.toLocalTime()
            ),
            (summary as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunset and sunrise soon`() = runTest {
        val now = unixEpochStart
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.HOURS)
        val sunPeriod = SunPeriod(
            listOf(
                SunMoment(firstMoment, event = SunEvent.Sunset),
                SunMoment(secondMoment, event = SunEvent.Sunrise)
            )
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(
                    firstMoment,
                    condition = Condition(1, false)
                )
            )
        )
        assertEquals(
            Sunset.WithSunriseSoon(
                time = firstMoment.toLocalTime(),
                sunrise = secondMoment.toLocalTime()
            ),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunrise soon but sunset in two days`() = runTest {
        val now = unixEpochStart
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.DAYS)
        val sunPeriod = SunPeriod(
            listOf(
                SunMoment(firstMoment, event = SunEvent.Sunrise),
                SunMoment(secondMoment, event = SunEvent.Sunset)
            )
        )

        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, condition = Condition(1, true))
            )
        )

        assertEquals(
            Sunrise.WithSunsetLater(
                time = firstMoment.toLocalTime(),
                sunset = secondMoment
            ),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunset soon but sunrise in two days`() = runTest {
        val now = unixEpochStart
        val firstMoment = now
        val secondMoment = now.plus(2, ChronoUnit.DAYS)
        val sunPeriod = SunPeriod(
            listOf(
                SunMoment(firstMoment, event = SunEvent.Sunset),
                SunMoment(secondMoment, event = SunEvent.Sunrise)
            )
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, condition = Condition(1, true))
            )
        )
        assertEquals(
            Sunset.WithSunriseLater(
                time = firstMoment.toLocalTime(),
                sunrise = secondMoment
            ),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunrise later`() = runTest {
        val now = unixEpochStart
        val firstMoment = now.plus(2, ChronoUnit.DAYS)
        val sunPeriod = SunPeriod(
            listOf(
                SunMoment(firstMoment, event = SunEvent.Sunrise),
            )
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, condition = Condition(1, true))
            )
        )
        assertEquals(
            Sunrise.Later(firstMoment),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `sunset later`() = runTest {
        val now = unixEpochStart
        val firstMoment = now.plus(2, ChronoUnit.DAYS)
        val sunPeriod = SunPeriod(
            listOf(
                SunMoment(firstMoment, event = SunEvent.Sunset),
            )
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, condition = Condition(1, true))
            )
        )
        assertEquals(
            Sunset.Later(firstMoment),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `night currently but no sunrise in sight`() = runTest {
        val now = unixEpochStart
        val sunPeriod = null
        val condPeriod = ConditionPeriod(List(48) {
            ConditionMoment(
                now.plus(it.toLong(), ChronoUnit.HOURS),
                condition = Condition(1, false)
            )
        })
        assertEquals(
            Sunrise.OutOfSight(Duration.ofHours(48)),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `day currently but no sunset in sight`() = runTest {
        val now = unixEpochStart
        val sunPeriod = null
        val condPeriod = ConditionPeriod(List(48) {
            ConditionMoment(
                now.plus(it.toLong(), ChronoUnit.HOURS),
                condition = Condition(1, true)
            )
        })
        assertEquals(
            Sunset.OutOfSight(Duration.ofHours(48)),
            (getSunSummary(now, sunPeriod, condPeriod) as ForecastResult.Success).data
        )
    }

    @Test
    fun `when no current desc returns outdated`() = runTest {
        val start = unixEpochStart
        val sunPeriod = null
        val condPeriod = ConditionPeriod(List(48) {
            ConditionMoment(
                start.plus(it.toLong(), ChronoUnit.HOURS),
                condition = Condition(1, false)
            )
        })
        val now = start.plus(48.toLong(), ChronoUnit.HOURS)
        assertEquals(ForecastResult.Outdated, getSunSummary(now, sunPeriod, condPeriod))
    }
}