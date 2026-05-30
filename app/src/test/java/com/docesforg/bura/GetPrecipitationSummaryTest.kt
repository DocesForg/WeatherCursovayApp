package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.PrecipitationMoment
import com.docesforg.bura.precipitation.PrecipitationPeriod
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import com.docesforg.bura.summary.precipitation.FuturePrecipitation
import com.docesforg.bura.summary.precipitation.PastPrecipitation
import com.docesforg.bura.summary.precipitation.PrecipitationSummary
import com.docesforg.bura.summary.precipitation.getPrecipitationSummary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GetPrecipitationSummaryTest {
    private fun dayOfPrecipitation(
        startTime: LocalDateTime,
        millimetersPerHour: Double
    ): List<PrecipitationMoment> =
        List(24) { hour ->
            PrecipitationMoment(
                hour = startTime.plus(hour.toLong(), ChronoUnit.HOURS),
                precipitation = MixedPrecipitation(
                    rain = Rain(
                        millimetersPerHour,
                        Precipitation.Unit.Millimeters
                    ), snow = Snow.ZeroMillimeters,
                    showers = Showers.ZeroMillimeters, unit = Precipitation.Unit.Millimeters
                )
            )
        }

    @Test
    fun `when past and future moments exist, past and future are correct`() = runTest {
        val startTime = unixEpochStart
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val middle = startTime.plus(8, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = middle, precipPeriod = period)
        assertEquals(
            ForecastResult.Success(
                PrecipitationSummary(
                    past = PastPrecipitation(
                        inHours = 8,
                        total = Rain(8.0, Precipitation.Unit.Millimeters)
                    ),
                    future = FuturePrecipitation.InHours(
                        inHours = 16,
                        total = Rain(16.0, Precipitation.Unit.Millimeters)
                    )
                )
            ),
            summary
        )
    }

    @Test
    fun `when no past moments, summary is outdated`() = runTest {
        val startTime = unixEpochStart
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val start = startTime.plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = start, precipPeriod = period)
        assertEquals(ForecastResult.Outdated, summary)
    }

    @Test
    fun `when no future moments, summary is outdated`() = runTest {
        val startTime = unixEpochStart
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val end = startTime.plus(24, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = end, precipPeriod = period)
        assertEquals(ForecastResult.Outdated, summary)
    }

    @Test
    fun `when no past or future moments, summary is outdated`() = runTest {
        val startTime = unixEpochStart
        val period = PrecipitationPeriod(dayOfPrecipitation(startTime, 1.0))
        val afterEnd = startTime.plus(3, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now = afterEnd, precipPeriod = period)
        assertEquals(ForecastResult.Outdated, summary)
    }

    @Test
    fun `when no precipitation in next 24 hours but on some future day, future describes that day`() =
        runTest {
            val startTime = unixEpochStart
            val period = PrecipitationPeriod(
                moments = buildList {
                    addAll(dayOfPrecipitation(startTime, 0.0))
                    addAll(
                        dayOfPrecipitation(
                            startTime.plus(1, ChronoUnit.DAYS),
                            0.0
                        )
                    )
                    addAll(
                        dayOfPrecipitation(
                            startTime.plus(2, ChronoUnit.DAYS),
                            1.0
                        )
                    )
                }
            )
            val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
            val summary = getPrecipitationSummary(now, period)
            assertEquals(
                FuturePrecipitation.OnDay(
                    onDay = Instant.ofEpochSecond(0).plus(2, ChronoUnit.DAYS)
                        .atZone(ZoneId.of("GMT")).toLocalDate(),
                    total = Rain(23.0, Precipitation.Unit.Millimeters)
                ),
                (summary as ForecastResult.Success).data.future
            )
        }

    @Test
    fun `when no precipitation in sight, future is none expected`() = runTest {
        val startTime = unixEpochStart
        val period = PrecipitationPeriod(
            moments = buildList {
                addAll(dayOfPrecipitation(startTime, 0.0))
                addAll(dayOfPrecipitation(startTime.plus(1, ChronoUnit.DAYS), 0.0))
                addAll(dayOfPrecipitation(startTime.plus(2, ChronoUnit.DAYS), 0.0))
            }
        )
        val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now, period)
        assertEquals(
            FuturePrecipitation.None(inDays = 1),
            (summary as ForecastResult.Success).data.future
        )
    }

    @Test
    fun `when no precipitation in next 24 hours and no days after, future has 0mm total`() =
        runTest {
            val startTime = unixEpochStart
            val period = PrecipitationPeriod(
                moments = buildList {
                    addAll(dayOfPrecipitation(startTime, 0.0))
                    addAll(
                        dayOfPrecipitation(
                            startTime.plus(1, ChronoUnit.DAYS),
                            0.0
                        )
                    )
                }
            )
            val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
            val summary = getPrecipitationSummary(now, period)
            assertEquals(
                FuturePrecipitation.InHours(
                    inHours = 24,
                    total = MixedPrecipitation(
                        rain = Rain.ZeroMillimeters,
                        snow = Snow.ZeroMillimeters,
                        showers = Showers.ZeroMillimeters,
                        unit = Precipitation.Unit.Millimeters
                    )
                ),
                (summary as ForecastResult.Success).data.future
            )
        }

    @Test
    fun `when precipitation in next 24 hours and after, future prioritizes 24 hours`() = runTest {
        val startTime = unixEpochStart
        val period = PrecipitationPeriod(
            moments = buildList {
                addAll(dayOfPrecipitation(startTime, 0.0))
                addAll(dayOfPrecipitation(startTime.plus(1, ChronoUnit.DAYS), 1.0))
                addAll(dayOfPrecipitation(startTime.plus(2, ChronoUnit.DAYS), 2.0))
            }
        )
        val now = startTime.plus(1, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES)
        val summary = getPrecipitationSummary(now, period)
        assertEquals(
            FuturePrecipitation.InHours(
                inHours = 24,
                total = Rain(24.0, Precipitation.Unit.Millimeters)
            ),
            (summary as ForecastResult.Success).data.future
        )
    }
}