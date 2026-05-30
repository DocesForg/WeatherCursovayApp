package com.docesforg.bura

import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.PrecipitationMoment
import com.docesforg.bura.precipitation.PrecipitationPeriod
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import org.junit.Assert.assertEquals
import org.junit.Test

class PrecipitationPeriodTest {
    @Test
    fun depth() {
        val period = PrecipitationPeriod(
            moments = listOf(
                PrecipitationMoment(
                    unixEpochStart,
                    MixedPrecipitation(
                        rain = Rain(1.0, Precipitation.Unit.Millimeters),
                        snow = Snow.ZeroMillimeters,
                        showers = Showers.ZeroMillimeters,
                        unit = Precipitation.Unit.Millimeters
                    ),
                )
            )
        )
        assertEquals(
            MixedPrecipitation(
                rain = Rain(1.0, Precipitation.Unit.Millimeters),
                snow = Snow.ZeroMillimeters,
                showers = Showers.ZeroMillimeters,
                unit = Precipitation.Unit.Millimeters
            ), period.total
        )
    }
}