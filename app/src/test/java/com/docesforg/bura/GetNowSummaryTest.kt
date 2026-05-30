package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.summary.now.NowSummary
import com.docesforg.bura.summary.now.getNowSummary
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetNowSummaryTest {
    @Test
    fun `summarizes current temperature, feels like and description and returns min and max temp of today`() =
        runTest {
            val firstDayFirstMoment = unixEpochStart.plus(22, ChronoUnit.HOURS)
            val now = firstDayFirstMoment.plus(10, ChronoUnit.MINUTES)
            val firstDaySecondMoment = firstDayFirstMoment.plus(1, ChronoUnit.HOURS)
            val secondDayFirstMoment = firstDaySecondMoment.plus(1, ChronoUnit.HOURS)
            val temperaturePeriod = TemperaturePeriod(
                moments = listOf(
                    TemperatureMoment(
                        firstDayFirstMoment,
                        Temperature(0.0, Temperature.Unit.DegreesCelsius)
                    ),
                    TemperatureMoment(
                        firstDaySecondMoment,
                        Temperature(1.0, Temperature.Unit.DegreesCelsius)
                    ),
                    TemperatureMoment(
                        secondDayFirstMoment,
                        Temperature(20.0, Temperature.Unit.DegreesCelsius)
                    )
                )
            )
            val feelsLikePeriod = TemperaturePeriod(
                moments = listOf(
                    TemperatureMoment(
                        firstDayFirstMoment,
                        Temperature(-1.0, Temperature.Unit.DegreesCelsius)
                    ),
                    TemperatureMoment(
                        firstDaySecondMoment,
                        Temperature(0.0, Temperature.Unit.DegreesCelsius)
                    ),
                    TemperatureMoment(
                        secondDayFirstMoment,
                        Temperature(20.0, Temperature.Unit.DegreesCelsius)
                    )
                )
            )
            val conditionPeriod = ConditionPeriod(
                moments = listOf(
                    ConditionMoment(firstDayFirstMoment, Condition(wmoCode = 1, isDay = false)),
                    ConditionMoment(firstDaySecondMoment, Condition(wmoCode = 2, isDay = false)),
                    ConditionMoment(secondDayFirstMoment, Condition(wmoCode = 3, isDay = true))
                )
            )
            val summary = getNowSummary(now, temperaturePeriod, feelsLikePeriod, conditionPeriod)
            assertEquals(
                ForecastResult.Success(
                    NowSummary(
                        temp = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                        feelsLike = Temperature(-1.0, Temperature.Unit.DegreesCelsius),
                        minTemp = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                        maxTemp = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                        cond = Condition(1, false)
                    )
                ),
                summary
            )
        }

    @Test
    fun `summary is outdated when no data after now`() = runTest {
        val firstMoment = unixEpochStart
        val afterFirstMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val now = afterFirstMoment.plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(0.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        val feelsLikePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(-1.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(firstMoment, Condition(wmoCode = 1, isDay = false)),
            )
        )
        assertEquals(ForecastResult.Outdated, getNowSummary(now, temperaturePeriod, feelsLikePeriod, conditionPeriod))
    }
}