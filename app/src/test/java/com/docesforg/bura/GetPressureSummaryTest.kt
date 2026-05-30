package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.pressure.PressureMoment
import com.docesforg.bura.pressure.PressurePeriod
import com.docesforg.bura.summary.pressure.PressureSummary
import com.docesforg.bura.summary.pressure.PressureTrend
import com.docesforg.bura.summary.pressure.getPressureSummary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetPressureSummaryTest {
    @Test
    fun `when at least one moment before now, returns now and trend`() = runTest {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = firstMoment,
                    pressure = Pressure(0.0, Pressure.Unit.Hectopascal)
                ),
                PressureMoment(
                    hour = secondMoment,
                    pressure = Pressure(1.0, Pressure.Unit.Hectopascal)
                )
            )
        )
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val summary = getPressureSummary(now, period)
        assertEquals(
            ForecastResult.Success(
                PressureSummary(
                    now = Pressure(1.0, Pressure.Unit.Hectopascal),
                    average = Pressure(0.5, Pressure.Unit.Hectopascal),
                    trend = PressureTrend.Rising
                )
            ),
            summary
        )
    }

    @Test
    fun `when no moments at now, summary is outdated`() = runTest {
        val firstMoment = unixEpochStart
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = firstMoment,
                    pressure = Pressure(1.0, Pressure.Unit.Hectopascal)
                )
            )
        )
        val now = firstMoment.plus(1, ChronoUnit.HOURS)
        assertEquals(
            ForecastResult.Outdated,
            getPressureSummary(now, period)
        )
    }

    @Test
    fun `when no moments before now, summary is outdated`() = runTest {
        val firstMoment = unixEpochStart
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(
                    hour = firstMoment,
                    pressure = Pressure(1.0, Pressure.Unit.Hectopascal)
                )
            )
        )
        val now = firstMoment
        val summary = getPressureSummary(now, period)
        assertEquals(ForecastResult.Outdated, summary)
    }
}