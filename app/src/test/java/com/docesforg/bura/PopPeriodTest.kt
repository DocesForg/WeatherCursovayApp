package com.docesforg.bura

import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.PopMoment
import com.docesforg.bura.pop.PopPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit
import kotlin.math.pow

class PopPeriodTest {
    @Test
    fun maximum() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PopPeriod(
            moments = listOf(
                PopMoment(firstMoment, pop = Pop(2.0)),
                PopMoment(secondMoment, pop = Pop(8.0)),
            )
        )
        assertEquals(Pop(8.0), period.maximum)
    }

    @Test
    fun once() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val thirdMoment = secondMoment.plus(1, ChronoUnit.HOURS)
        val period = PopPeriod(
            moments = listOf(
                PopMoment(firstMoment, pop = Pop(5.0)),
                PopMoment(secondMoment, pop = Pop(5.0)),
                PopMoment(thirdMoment, pop = Pop(5.0))
            )
        )
        assertEquals(Pop((1 - 0.95.pow(3)) * 100), period.once)
    }
}