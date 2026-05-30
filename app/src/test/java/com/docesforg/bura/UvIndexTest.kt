package com.docesforg.bura

import com.docesforg.bura.uvindex.UvIndex
import org.junit.Assert.*
import org.junit.Test

class UvIndexTest {
    @Test
    fun risk() {
        assertEquals(UvIndex.Risk.Low, UvIndex(value = 0).risk)
        assertEquals(UvIndex.Risk.Moderate, UvIndex(value = 3).risk)
        assertEquals(UvIndex.Risk.High, UvIndex(value = 6).risk)
        assertEquals(UvIndex.Risk.VeryHigh, UvIndex(value = 8).risk)
        assertEquals(UvIndex.Risk.Extreme, UvIndex(value = 19).risk)
    }

    @Test
    fun equals() {
        assertEquals(UvIndex(1), UvIndex(1))
    }

    @Test
    fun comparison() {
        assertTrue(UvIndex(1) > UvIndex(0))
    }
}