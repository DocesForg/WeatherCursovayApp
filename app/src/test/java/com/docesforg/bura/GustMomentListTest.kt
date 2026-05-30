package com.docesforg.bura

import com.docesforg.bura.gust.GustMoment
import com.docesforg.bura.gust.GustPeriod
import com.docesforg.bura.wind.WindSpeed
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class GustMomentListTest {
    @Test
    fun maximum() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = GustPeriod(
            moments = listOf(
                GustMoment(firstMoment, WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond)),
                GustMoment(secondMoment, WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond))
            )
        )
        assertEquals(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), period.maximum)
    }
}