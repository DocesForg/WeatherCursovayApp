package com.docesforg.bura

import com.docesforg.bura.wind.WindSpeed
import org.junit.Assert.*
import org.junit.Test

class WindSpeedTest {
    @Test
    fun `get bft and convert mps to kph, mph and kn`() {
        val mps = WindSpeed(3.0, WindSpeed.Unit.MetersPerSecond)
        assertEquals(3.0, mps.value, 0.0)
        assertEquals(WindSpeed.Unit.MetersPerSecond, mps.unit)
        assertEquals(2, mps.beaufort)

        val kmh = mps.convertTo(WindSpeed.Unit.KilometersPerHour)
        assertEquals(10.8, kmh.value, 0.0)
        assertEquals(WindSpeed.Unit.KilometersPerHour, kmh.unit)
        assertEquals(2, kmh.beaufort)

        val mph = kmh.convertTo(WindSpeed.Unit.MilesPerHour)
        assertEquals(6.71, mph.value, 0.01)
        assertEquals(WindSpeed.Unit.MilesPerHour, mph.unit)
        assertEquals(2, mph.beaufort)

        val kn = mph.convertTo(WindSpeed.Unit.Knots)
        assertEquals(5.83, kn.value, 0.01)
        assertEquals(WindSpeed.Unit.Knots, kn.unit)
        assertEquals(2, kn.beaufort)
    }

    @Test
    fun `greater than`() {
        val speedLess = WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
        val speedGreater = WindSpeed(2.0, WindSpeed.Unit.MetersPerSecond)
        speedGreater.convertTo(WindSpeed.Unit.KilometersPerHour)
        assertTrue(speedGreater > speedLess)
    }

    @Test
    fun equals() {
        val one = WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
        val two = WindSpeed(1.0, WindSpeed.Unit.MetersPerSecond)
        one.convertTo(WindSpeed.Unit.MilesPerHour)
        assertTrue(one == two)
    }
}