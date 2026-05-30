package com.docesforg.bura

import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.pressure.PressureMoment
import com.docesforg.bura.pressure.PressurePeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class PressurePeriodTest {
    @Test
    fun minimum() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(hour = firstMoment, Pressure(1000.0, Pressure.Unit.Hectopascal)),
                PressureMoment(hour = secondMoment, Pressure(1000.0, Pressure.Unit.Hectopascal))
            )
        )
        assertEquals(Pressure(1000.0, Pressure.Unit.Hectopascal), period.minimum)
    }

    @Test
    fun average() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = PressurePeriod(
            moments = listOf(
                PressureMoment(firstMoment, Pressure(1000.0, Pressure.Unit.Hectopascal)),
                PressureMoment(secondMoment, Pressure(1010.0, Pressure.Unit.Hectopascal))
            )
        )
        assertEquals(Pressure(1005.0, Pressure.Unit.Hectopascal), period.average)
    }
}