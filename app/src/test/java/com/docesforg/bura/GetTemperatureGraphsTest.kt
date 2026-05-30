package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.graphs.common.GraphTime
import com.docesforg.bura.graphs.temperature.GraphTemperature
import com.docesforg.bura.graphs.temperature.TemperatureGraph
import com.docesforg.bura.graphs.temperature.TemperatureGraphPoint
import com.docesforg.bura.graphs.temperature.getTemperatureGraphs
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class GetTemperatureGraphsTest {
    @Test
    fun `combines data into graph points and extracts min max temps`() = runTest {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
                ConditionMoment(thirdMoment, Condition(3, false))
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(0.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val result = (getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        ) as ForecastResult.Success).data.graphs.first()
        assertEquals(
            TemperatureGraph(
                day = LocalDate.parse("1970-01-01"),
                points = listOf(
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("00:00"),
                            meta = GraphTime.Meta.Past
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Minimum
                        ),
                        condition = Condition(1, true),

                        ),
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("01:00"),
                            meta = GraphTime.Meta.Present
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Regular
                        ),
                        condition = Condition(2, false),

                        ),
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("02:00"),
                            meta = GraphTime.Meta.Future
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Maximum
                        ),
                        condition = Condition(3, false),

                        )
                )
            ),
            result
        )
    }

    @Test
    fun `when all temps the same, min max equals the first temperature`() = runTest {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
                ConditionMoment(thirdMoment, Condition(3, false))
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val result = (getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        ) as ForecastResult.Success).data.graphs.first()
        assert(result.points.all { it.temperature.meta == GraphTemperature.Meta.Regular })
    }

    @Test
    fun `minimum takes the last min moment`() = runTest {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
                ConditionMoment(thirdMoment, Condition(3, false))
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(thirdMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius))
            )
        )
        val result = (getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        ) as ForecastResult.Success).data.graphs.first()
        assertEquals(
            LocalTime.parse("01:00"),
            result.points.first { it.temperature.meta == GraphTemperature.Meta.Minimum }.time.value
        )
    }

    @Test
    fun `first data point of next day is included in the graph`() = runTest {
        val firstMoment = unixEpochStart.plus(23, ChronoUnit.HOURS)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val now = firstMoment.plus(10, ChronoUnit.MINUTES)
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(firstMoment, Condition(1, true)),
                ConditionMoment(secondMoment, Condition(2, false)),
            )
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(firstMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        val result = (getTemperatureGraphs(
            now,
            tempPeriod,
            condPeriod
        ) as ForecastResult.Success).data.graphs.first()
        assertEquals(
            TemperatureGraph(
                day = LocalDate.parse("1970-01-01"),
                points = listOf(
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("23:00"),
                            meta = GraphTime.Meta.Present
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Regular
                        ),

                        condition = Condition(1, true)
                    ),
                    TemperatureGraphPoint(
                        time = GraphTime(
                            value = LocalTime.parse("00:00"),
                            meta = GraphTime.Meta.Future
                        ),
                        temperature = GraphTemperature(
                            value = Temperature(1.0, Temperature.Unit.DegreesCelsius),
                            meta = GraphTemperature.Meta.Regular
                        ),
                        condition = Condition(2, false)
                    )
                )
            ),
            result
        )
    }
}