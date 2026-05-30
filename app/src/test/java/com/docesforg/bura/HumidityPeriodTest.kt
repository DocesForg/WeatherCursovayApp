package com.docesforg.bura

import com.docesforg.bura.humidity.Humidity
import com.docesforg.bura.humidity.HumidityMoment
import com.docesforg.bura.humidity.HumidityPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class HumidityPeriodTest {
    @Test
    fun average() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = HumidityPeriod(
            moments = listOf(
                HumidityMoment(firstMoment, Humidity(50.0)),
                HumidityMoment(secondMoment, Humidity(90.0))
            )
        )
        assertEquals(Humidity(70.0), period.average)
    }
}