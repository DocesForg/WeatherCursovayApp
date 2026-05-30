package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.gust.GustMoment
import com.docesforg.bura.gust.GustPeriod
import com.docesforg.bura.summary.wind.WindSummary
import com.docesforg.bura.summary.wind.getWindSummary
import com.docesforg.bura.wind.Wind
import com.docesforg.bura.wind.WindDirection
import com.docesforg.bura.wind.WindMoment
import com.docesforg.bura.wind.WindPeriod
import com.docesforg.bura.wind.WindSpeed
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class WindSummaryTest {
    @Test
    fun `gets current wind speed, direction and gust speed`() = runTest {
        val time = unixEpochStart
        val now = time.plus(10, ChronoUnit.MINUTES)
        val windPeriod = WindPeriod(
            listOf(
                WindMoment(
                    time,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                )
            )
        )
        val gustPeriod = GustPeriod(listOf(
            GustMoment(
                time,
                WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
            )))
        val summary = getWindSummary(now, windPeriod, gustPeriod)
        assertEquals(
            ForecastResult.Success(
                WindSummary(
                    windNow = Wind(
                        WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond),
                        WindDirection(0.0)
                    ),
                    gustNow = WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
                )
            ),
            summary
        )
    }

    @Test
    fun `outdated when no now`() = runTest {
        val time = unixEpochStart
        val now = time.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val windPeriod = WindPeriod(
            listOf(
                WindMoment(
                    time,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                )
            )
        )
        val gustPeriod = GustPeriod(listOf(
            GustMoment(
                time,
                WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
            )))
        val summary = getWindSummary(now, windPeriod, gustPeriod)
        assertEquals(ForecastResult.Outdated, summary)
    }
}