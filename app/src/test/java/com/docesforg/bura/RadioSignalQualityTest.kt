package com.docesforg.bura

import com.docesforg.bura.radio.calculateSignalQualityPercent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioSignalQualityTest {
    @Test
    fun `quality percent is calculated from path loss`() {
        val strongSignal = calculateSignalQualityPercent(100.0)
        val weakSignal = calculateSignalQualityPercent(150.0)

        assertTrue(strongSignal > weakSignal)
        assertEquals(87, strongSignal)
        assertEquals(26, weakSignal)
    }

    @Test
    fun `quality percent is clamped to display range`() {
        assertEquals(99, calculateSignalQualityPercent(70.0))
        assertEquals(1, calculateSignalQualityPercent(200.0))
    }
}
