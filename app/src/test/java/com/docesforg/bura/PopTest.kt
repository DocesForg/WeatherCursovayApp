package com.docesforg.bura

import com.docesforg.bura.pop.Pop
import org.junit.Assert.*
import org.junit.Test

class PopTest {
    @Test
    fun equals() {
        assertEquals(Pop(1.0), Pop(1.0))
    }

    @Test
    fun compare() {
        assertTrue(Pop(1.0) > Pop(0.0))
    }
}