package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.graphs.precipitation.PrecipitationTotal
import com.docesforg.bura.graphs.precipitation.TotalPrecipitationInHours
import com.docesforg.bura.graphs.precipitation.getPrecipitationTotals
import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.PrecipitationMoment
import com.docesforg.bura.precipitation.PrecipitationPeriod
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GetPrecipitationTotalsTest {
    @Test
    fun `generates last and future for today and future for other days`() = runTest {
        val startOfFirstDay = unixEpochStart
        val startOfSecondDay = startOfFirstDay.plus(1, ChronoUnit.DAYS)
        val period = PrecipitationPeriod(buildList {
            for (i in 0..23) {
                add(
                    PrecipitationMoment(
                        hour = startOfFirstDay.plus(i.toLong(), ChronoUnit.HOURS),
                        precipitation = MixedPrecipitation(
                            rain = Rain(1.0, Precipitation.Unit.Millimeters),
                            snow = Snow(5.0, Precipitation.Unit.Millimeters),
                            showers = Showers.ZeroMillimeters,
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                )
            }
            for (i in 0..23) {
                add(
                    PrecipitationMoment(
                        hour = startOfSecondDay.plus(i.toLong(), ChronoUnit.HOURS),
                        precipitation = MixedPrecipitation(
                            rain = Rain.ZeroMillimeters,
                            snow = Snow.ZeroMillimeters,
                            showers = Showers(2.0, Precipitation.Unit.Millimeters),
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                )
            }
        })
        val totals = (getPrecipitationTotals(
            startOfFirstDay.plus(8, ChronoUnit.HOURS),
            period
        ) as ForecastResult.Success).data
        assertEquals(
            listOf(
                PrecipitationTotal.Today(
                    day = LocalDate.parse("1970-01-01"),
                    past = TotalPrecipitationInHours(
                        hours = 8,
                        total = MixedPrecipitation(
                            rain = Rain(8.0, Precipitation.Unit.Millimeters),
                            snow = Snow(40.0, Precipitation.Unit.Millimeters),
                            showers = Showers.ZeroMillimeters,
                            unit = Precipitation.Unit.Millimeters
                        ),
                    ),
                    future = TotalPrecipitationInHours(
                        hours = 24,
                        total = MixedPrecipitation(
                            rain = Rain(16.0, Precipitation.Unit.Millimeters),
                            snow = Snow(80.0, Precipitation.Unit.Millimeters),
                            showers = Showers(16.0, Precipitation.Unit.Millimeters),
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                ),
                PrecipitationTotal.OtherDay(
                    day = LocalDate.parse("1970-01-02"),
                    total = Showers(48.0, Precipitation.Unit.Millimeters)
                )
            ),
            totals
        )
    }
}