package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.humidity.Humidity
import com.docesforg.bura.humidity.HumidityMoment
import com.docesforg.bura.humidity.HumidityPeriod
import com.docesforg.bura.summary.humidity.HumiditySummary
import com.docesforg.bura.summary.humidity.getHumiditySummary
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class HumiditySummaryTest {
    @Test
    fun `gets humidity and dew point of now`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val humidityPeriod = HumidityPeriod(listOf(HumidityMoment(firstMoment, Humidity(0.0))))
        val dewPointPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        assertEquals(
            ForecastResult.Success(
                HumiditySummary(
                    humidityNow = Humidity(0.0),
                    dewPointNow = Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            ),
            getHumiditySummary(now, humidityPeriod, dewPointPeriod)
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val humidityPeriod = HumidityPeriod(listOf(HumidityMoment(firstMoment, Humidity(0.0))))
        val dewPointPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        assertEquals(ForecastResult.Outdated, getHumiditySummary(now, humidityPeriod, dewPointPeriod))
    }
}