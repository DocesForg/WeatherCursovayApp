package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.summary.feelslike.FeelsLikeSummary
import com.docesforg.bura.summary.feelslike.FeelsVsActual
import com.docesforg.bura.summary.feelslike.getFeelsLikeSummary
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetFeelsLikeSummaryTest {
    @Test
    fun `gets now and describes what it feels like`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(-1.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        assertEquals(
            ForecastResult.Success(
                FeelsLikeSummary(
                    feelsLikeNow = Temperature(-1.0, Temperature.Unit.DegreesCelsius),
                    actualNow = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                    vsActual = FeelsVsActual.Colder
                )
            ),
            getFeelsLikeSummary(now, tempPeriod = temperaturePeriod, feelsPeriod = feelsLikePeriod)
        )
    }

    @Test
    fun `when feels like and actual within 1 degree of each other feel is similar`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(-0.5, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        assertEquals(
            ForecastResult.Success(
                FeelsLikeSummary(
                    feelsLikeNow = Temperature(-0.5, Temperature.Unit.DegreesCelsius),
                    actualNow = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                    vsActual = FeelsVsActual.Similar
                )
            ),
            getFeelsLikeSummary(now, tempPeriod = temperaturePeriod, feelsPeriod = feelsLikePeriod)
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val feelsLikePeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        assertEquals(ForecastResult.Outdated, getFeelsLikeSummary(now, tempPeriod = temperaturePeriod, feelsPeriod = feelsLikePeriod))
    }
}