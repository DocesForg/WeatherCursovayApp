package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.graphs.common.GraphTime
import com.docesforg.bura.graphs.pop.GraphPop
import com.docesforg.bura.graphs.pop.PopGraph
import com.docesforg.bura.graphs.pop.PopGraphPoint
import com.docesforg.bura.graphs.pop.getPopGraphs
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.PopMoment
import com.docesforg.bura.pop.PopPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class GetPopGraphsTest {
    @Test
    fun `constructs pop graphs`() = runTest {
        val firstMoment = unixEpochStart.plus(22, ChronoUnit.HOURS)
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val now = secondMoment
        val popPeriod = PopPeriod(
            listOf(
                PopMoment(hour = firstMoment, pop = Pop(0.0)),
                PopMoment(hour = secondMoment, pop = Pop(0.0)),
                PopMoment(hour = thirdMoment, pop = Pop(1.0))
            )
        )
        val conditionPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(hour = firstMoment, condition = Condition(0, true)),
                ConditionMoment(hour = secondMoment, condition = Condition(1, true)),
                ConditionMoment(hour = thirdMoment, condition = Condition(2, true))
            )
        )
        val graphs = (getPopGraphs(now, popPeriod, conditionPeriod) as ForecastResult.Success).data
        assertEquals(
            listOf(
                PopGraph(
                    day = LocalDate.parse("1970-01-01"),
                    points = listOf(
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("22:00"),
                                meta = GraphTime.Meta.Past
                            ),
                            pop = GraphPop(
                                Pop(0.0),
                                meta = GraphPop.Meta.Regular
                            ),
                            condition = Condition(0, true)
                        ),
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("23:00"),
                                meta = GraphTime.Meta.Present
                            ),
                            pop = GraphPop(
                                Pop(0.0),
                                meta = GraphPop.Meta.Regular
                            ),
                            condition = Condition(1, true)
                        ),
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("00:00"),
                                meta = GraphTime.Meta.Future
                            ),
                            pop = GraphPop(
                                Pop(1.0),
                                meta = GraphPop.Meta.Maximum
                            ),
                            condition = Condition(2, true)
                        )
                    )
                ),
                PopGraph(
                    day = LocalDate.parse("1970-01-02"),
                    points = listOf(
                        PopGraphPoint(
                            time = GraphTime(
                                value = LocalTime.parse("00:00"),
                                meta = GraphTime.Meta.Future
                            ),
                            pop = GraphPop(
                                Pop(1.0),
                                meta = GraphPop.Meta.Maximum
                            ),
                            condition = Condition(2, true)
                        )
                    )
                )
            ),
            graphs
        )
    }
}