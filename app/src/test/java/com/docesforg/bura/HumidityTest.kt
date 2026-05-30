package com.docesforg.bura

import com.docesforg.bura.humidity.Humidity
import org.junit.Assert.*
import org.junit.Test

class HumidityTest {
    @Test
    fun equals() {
        assertEquals(Humidity(1.0), Humidity(1.0))
    }

    @Test
    fun compare() {
        assertTrue(Humidity(1.0) > Humidity(0.0))
    }

    @Test
    fun plus() {
        assertEquals(Humidity(1.0) + Humidity(1.0), Humidity(2.0))
    }

    @Test
    fun divide() {
        assertEquals(Humidity(50.0) / 2, Humidity(25.0))
    }
}