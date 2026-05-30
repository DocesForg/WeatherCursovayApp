package com.docesforg.bura

import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import org.junit.Assert.assertEquals
import org.junit.Test

class PrecipitationTest {
    @Test
    fun `sums rain showers and snow`() {
        val rain = Rain(10.0, Precipitation.Unit.Millimeters)
        val showers = Showers(0.0, Precipitation.Unit.Millimeters)
        val snow = Snow(70.0, Precipitation.Unit.Millimeters)
        val sum = MixedPrecipitation(
            rain = rain,
            snow = snow,
            showers = showers,
            unit = Precipitation.Unit.Millimeters
        ).reduce()
        sum as MixedPrecipitation
        assertEquals(20.0, sum.value, 0.01)
        assertEquals(rain, sum.rain)
        assertEquals(showers, sum.showers)
        assertEquals(snow, sum.snow)
    }

    @Test
    fun `converts to inches`() {
        val rain = Rain(10.0, Precipitation.Unit.Millimeters)
        val showers = Showers(0.0, Precipitation.Unit.Millimeters)
        val snow = Snow(70.0, Precipitation.Unit.Millimeters)
        val sum = MixedPrecipitation(
            rain = rain,
            snow = snow,
            showers = showers,
            unit = Precipitation.Unit.Millimeters
        )
            .convertTo(Precipitation.Unit.Inches)
        assertEquals(
            0.787,
            sum.value,
            0.001
        )
    }

    @Test
    fun equals() {
        val rain = Rain(10.0, Precipitation.Unit.Millimeters)
        val showers = Showers(0.0, Precipitation.Unit.Millimeters)
        val snow = Snow(70.0, Precipitation.Unit.Millimeters)
        val one = MixedPrecipitation(
            rain = rain,
            snow = snow,
            showers = showers,
            unit = Precipitation.Unit.Millimeters
        )
        val two = MixedPrecipitation(
            rain = rain,
            snow = snow,
            showers = showers,
            unit = Precipitation.Unit.Millimeters
        )
        assertEquals(one, two)
    }

    @Test
    fun plus() {
        val rainOne = Rain(10.0, Precipitation.Unit.Millimeters)
        val showersOne = Showers.ZeroMillimeters
        val snowOne = Snow(70.0, Precipitation.Unit.Millimeters)
        val rainTwo = Rain(5.0, Precipitation.Unit.Millimeters)
        val showersTwo = Showers(10.0, Precipitation.Unit.Millimeters)
        val snowTwo = Snow.ZeroMillimeters
        val one = MixedPrecipitation(
            rain = rainOne,
            snow = snowOne,
            showers = showersOne,
            unit = Precipitation.Unit.Millimeters
        )
            .convertTo(Precipitation.Unit.Inches)
        val two = MixedPrecipitation(
            rain = rainTwo,
            snow = snowTwo,
            showers = showersTwo,
            unit = Precipitation.Unit.Millimeters
        )
        val sum = (one + two).reduce()
        sum as MixedPrecipitation
        assertEquals(1.37, sum.value, 0.01)
    }

    @Test
    fun `snow calculates liquid value properly from inches`() {
        val snowIn = Snow(1.0, Precipitation.Unit.Inches)
        val snowMm = snowIn.convertTo(Precipitation.Unit.Millimeters)
        assertEquals(
            25.4 / 7,
            snowMm.liquidValue,
            0.01
        )
    }
}