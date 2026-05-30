package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.PopMoment
import com.docesforg.bura.pop.PopPeriod
import com.docesforg.bura.summary.daily.DailySummary
import com.docesforg.bura.summary.daily.DaySummary
import com.docesforg.bura.summary.daily.getDailySummary
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetDailySummaryTest {
    @Test
    fun `groups moments into days and summarizes them`() = runTest {
        val firstDayFirstMoment = unixEpochStart.plus(21, ChronoUnit.HOURS)
        val firstDaySecondMoment = firstDayFirstMoment.plus(1, ChronoUnit.HOURS)
        val firstDayThirdMoment = firstDaySecondMoment.plus(1, ChronoUnit.HOURS)
        val secondDayFirstMoment = firstDayThirdMoment.plus(1, ChronoUnit.HOURS)
        val secondDaySecondMoment = secondDayFirstMoment.plus(1, ChronoUnit.HOURS)
        val now = secondDayFirstMoment.plus(10, ChronoUnit.MINUTES)
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
                    firstDayThirdMoment,
                    Temperature(2.0, Temperature.Unit.DegreesCelsius)
                ),
                TemperatureMoment(
                    secondDayFirstMoment,
                    Temperature(3.0, Temperature.Unit.DegreesCelsius)
                ),
                TemperatureMoment(
                    secondDaySecondMoment,
                    Temperature(4.0, Temperature.Unit.DegreesCelsius)
                )
            )
        )
        val conditionPeriod = ConditionPeriod(
            moments = listOf(
                ConditionMoment(
                    firstDayFirstMoment,
                    Condition(wmoCode = 10, isDay = true)
                ),
                ConditionMoment(
                    firstDaySecondMoment,
                    Condition(wmoCode = 2, isDay = true)
                ),
                ConditionMoment(
                    firstDayThirdMoment,
                    Condition(wmoCode = 3, isDay = false)
                ),
                ConditionMoment(
                    secondDayFirstMoment,
                    Condition(wmoCode = 4, isDay = false)
                ),
                ConditionMoment(
                    secondDaySecondMoment,
                    Condition(wmoCode = 5, isDay = false)
                ),
            )
        )
        val popPeriod = PopPeriod(
            moments = listOf(
                PopMoment(firstDayFirstMoment, Pop(5.0)),
                PopMoment(firstDaySecondMoment, Pop(5.0)),
                PopMoment(firstDayThirdMoment, Pop(5.0)),
                PopMoment(secondDayFirstMoment, Pop(5.0)),
                PopMoment(secondDaySecondMoment, Pop(5.0)),
            )
        )
        val summary = getDailySummary(now, temperaturePeriod, conditionPeriod, popPeriod)
        assertEquals(
            ForecastResult.Success(
                DailySummary(
                    minTemp = Temperature(3.0, Temperature.Unit.DegreesCelsius),
                    maxTemp = Temperature(4.0, Temperature.Unit.DegreesCelsius),
                    days = listOf(
                        DaySummary(
                            isToday = true,
                            time = secondDayFirstMoment.atZone(ZoneId.of("GMT")).toLocalDate(),
                            tempNow = Temperature(3.0, Temperature.Unit.DegreesCelsius),
                            min = Temperature(3.0, Temperature.Unit.DegreesCelsius),
                            max = Temperature(4.0, Temperature.Unit.DegreesCelsius),
                            desc = Condition(wmoCode = 5, isDay = false),
                            pop = Pop(5.0)
                        )
                    )
                )
            ),
            summary
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
        val popPeriod = PopPeriod(
            listOf(
                PopMoment(
                    firstMoment,
                    Pop(1.0)
                )
            )
        )
        val conditionPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(
                    firstMoment,
                    Condition(wmoCode = 1, isDay = true)
                )
            )
        )
        val summary = getDailySummary(now, temperaturePeriod, conditionPeriod, popPeriod)
        assertEquals(ForecastResult.Outdated, summary)
    }
}