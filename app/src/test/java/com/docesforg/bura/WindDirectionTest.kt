package com.docesforg.bura

import com.docesforg.bura.wind.WindDirection
import org.junit.Assert.*
import org.junit.Test

class WindDirectionTest {
    @Test
    fun `cardinal direction`() {
        assertEquals(WindDirection.Compass.N, WindDirection(0.0).compass)
        assertEquals(WindDirection.Compass.ESE, WindDirection(112.0).compass)
        assertEquals(WindDirection.Compass.SW, WindDirection(225.0).compass)
        assertEquals(WindDirection.Compass.NNW, WindDirection(338.0).compass)
    }

    @Test
    fun `normalizes degrees to 0-359`() {
        assertEquals(WindDirection(30.0), WindDirection(350.0 + 40.0))
        assertEquals(WindDirection(0.0), WindDirection(360.0))
        assertEquals(WindDirection(1.0), WindDirection(361.0))
        assertEquals(WindDirection(359.0), WindDirection(359.0))
    }

    @Test
    fun equals() {
        assertEquals(WindDirection(0.0), WindDirection(0.0))
    }
}