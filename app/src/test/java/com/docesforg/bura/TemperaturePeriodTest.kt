package com.docesforg.bura

import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class TemperaturePeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = TemperaturePeriod(
            moments = listOf(
                TemperatureMoment(firstMoment, Temperature(1.0, Temperature.Unit.DegreesCelsius)),
                TemperatureMoment(secondMoment, Temperature(2.0, Temperature.Unit.DegreesCelsius)),
            )
        )
        assertEquals(Temperature(1.0, Temperature.Unit.DegreesCelsius), period.minimum)
        assertEquals(Temperature(2.0, Temperature.Unit.DegreesCelsius), period.maximum)
    }
}