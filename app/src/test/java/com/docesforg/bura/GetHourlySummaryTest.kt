package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.PopMoment
import com.docesforg.bura.pop.PopPeriod
import com.docesforg.bura.summary.hourly.HourSummary
import com.docesforg.bura.summary.hourly.getHourlySummary
import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.sun.SunMoment
import com.docesforg.bura.sun.SunPeriod
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetHourlySummaryTest {
    @Test
    fun `combines weather and sun data and arranges it chronologically`() = runTest {
        val startOfTime = unixEpochStart.plus(5, ChronoUnit.DAYS)
        val firstMoment = startOfTime.plus(1, ChronoUnit.HOURS)
        val secondMoment = startOfTime.plus(2, ChronoUnit.HOURS)
        val thirdMoment = startOfTime.plus(3, ChronoUnit.HOURS)
        val sunriseMoment = firstMoment.plus(30, ChronoUnit.MINUTES)
        val sunsetMoment = secondMoment.plus(30, ChronoUnit.MINUTES)
        val pastSunsetMoment = sunsetMoment.minus(1, ChronoUnit.DAYS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(0.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(firstMoment, pop = Pop(0.0)),
                PopMoment(secondMoment, pop = Pop(10.0)),
                PopMoment(thirdMoment, pop = Pop(10.0))
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = false)
                ),
                ConditionMoment(
                    secondMoment,
                    Condition(wmoCode = 1, isDay = true)
                ),
                ConditionMoment(
                    thirdMoment,
                    Condition(wmoCode = 1, isDay = false)
                )
            )
        )
        val sunPeriod = SunPeriod(
            moments = listOf(
                SunMoment(pastSunsetMoment, SunEvent.Sunset),
                SunMoment(sunriseMoment, SunEvent.Sunrise),
                SunMoment(sunsetMoment, SunEvent.Sunset)
            )
        )
        val summary = getHourlySummary(now, temperaturePeriod, popPeriod, conditionPeriod, sunPeriod)
        assertEquals(
            ForecastResult.Success(
                listOf(
                    HourSummary.Weather(
                        time = firstMoment,
                        isNow = true,
                        temp = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                        desc = Condition(wmoCode = 1, isDay = false),
                        pop = null
                    ),
                    HourSummary.Sun(
                        time = sunriseMoment,
                        event = SunEvent.Sunrise
                    ),
                    HourSummary.Weather(
                        time = secondMoment,
                        isNow = false,
                        temp = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                        desc = Condition(wmoCode = 1, isDay = true),
                        pop = Pop(10.0)
                    ),
                    HourSummary.Sun(
                        time = sunsetMoment,
                        event = SunEvent.Sunset
                    ),
                    HourSummary.Weather(
                        time = thirdMoment,
                        isNow = false,
                        temp = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                        desc = Condition(wmoCode = 1, isDay = false),
                        pop = Pop(10.0)
                    ),
                )
            ),
            summary
        )
    }

    @Test
    fun `summary is outdated when no data from now`() = runTest {
        val firstMoment = unixEpochStart
        val now = firstMoment.plus(1, ChronoUnit.HOURS)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(1.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(
                    firstMoment,
                    pop = Pop(10.0)
                )
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = true)
                )
            )
        )
        val summary = getHourlySummary(now, temperaturePeriod, popPeriod, conditionPeriod, null)
        assertEquals(ForecastResult.Outdated, summary)
    }

    @Test
    fun `no sun data when no sun moments from now`() = runTest {
        val startOfTime = unixEpochStart
        val firstMoment = startOfTime.plus(10, ChronoUnit.HOURS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val pastSunrise = firstMoment.minus(3, ChronoUnit.HOURS)
        val pastSunset = firstMoment.minus(2, ChronoUnit.HOURS)
        val temperaturePeriod = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(
                    firstMoment,
                    Temperature(0.0, Temperature.Unit.DegreesCelsius)
                ),
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(
                    firstMoment,
                    pop = Pop(0.0)
                ),
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = false)
                ),
            )
        )
        val sunPeriod = SunPeriod(
            moments = listOf(
                SunMoment(time = pastSunrise, event = SunEvent.Sunrise),
                SunMoment(time = pastSunset, event = SunEvent.Sunset)
            )
        )
        val summary = getHourlySummary(now, temperaturePeriod, popPeriod, conditionPeriod, sunPeriod)
        assertEquals(
            ForecastResult.Success(
                listOf(
                    HourSummary.Weather(
                        time = firstMoment.atZone(ZoneId.of("GMT")).toLocalDateTime(),
                        isNow = true,
                        temp = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                        desc = Condition(wmoCode = 1, isDay = false),
                        pop = null
                    ),
                ),
            ),
            summary
        )
    }
}