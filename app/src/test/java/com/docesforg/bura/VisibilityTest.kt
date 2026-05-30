package com.docesforg.bura

import com.docesforg.bura.visibility.Visibility
import org.junit.Assert.*
import org.junit.Test

class VisibilityTest {
    @Test
    fun `convert to kilometers and miles`() {
        val visibility = Visibility(1000.0, Visibility.Unit.Meters)
        assertEquals(1000.0, visibility.value, 0.0)
        assertEquals(Visibility.Unit.Meters, visibility.unit)

        val km = visibility.convertTo(Visibility.Unit.Kilometers)
        assertEquals(1.0, km.value, 0.0)
        assertEquals(Visibility.Unit.Kilometers, km.unit)

        val mi = visibility.convertTo(Visibility.Unit.Miles)
        assertEquals(0.62, mi.value, 0.01)
        assertEquals(Visibility.Unit.Miles, mi.unit)
    }

    @Test
    fun equals() {
        val one = Visibility(1.0, Visibility.Unit.Meters)
        val two = Visibility(1.0, Visibility.Unit.Meters)
        two.convertTo(Visibility.Unit.Kilometers)
        assertEquals(one, two)
    }

    @Test
    fun `greater than`() {
        val less = Visibility(1.0, Visibility.Unit.Meters)
        val greater = Visibility(2.0, Visibility.Unit.Meters)
        greater.convertTo(Visibility.Unit.Kilometers)
        assertTrue(greater > less)
    }

    @Test
    fun `smart kilometers`() {
        val visibility = Visibility(90.0, Visibility.Unit.Meters)
        visibility.convertTo(Visibility.Unit.Kilometers)
        assertEquals(90.0, visibility.value, 0.0)
        assertEquals(Visibility.Unit.Meters, visibility.unit)
    }

    @Test
    fun `smart miles`() {
        val visibility = Visibility(150.0, Visibility.Unit.Meters)
        val miles = visibility.convertTo(Visibility.Unit.Miles)
        assertEquals(492.12, miles.value, 0.01)
        assertEquals(Visibility.Unit.Feet, miles.unit)
    }
}