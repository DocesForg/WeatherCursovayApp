package com.docesforg.bura

import com.docesforg.bura.wind.Wind
import com.docesforg.bura.wind.WindDirection
import com.docesforg.bura.wind.WindMoment
import com.docesforg.bura.wind.WindPeriod
import com.docesforg.bura.wind.WindSpeed
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class WindPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = WindPeriod(
            moments = listOf(
                WindMoment(
                    firstMoment,
                    Wind(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                ),
                WindMoment(
                    secondMoment,
                    Wind(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), WindDirection(0.0))
                ),
            )
        )
        assertEquals(WindSpeed(0.0, WindSpeed.Unit.MetersPerSecond), period.minimumSpeed)
        assertEquals(WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond), period.maximumSpeed)
    }
}