package com.docesforg.bura

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.summary.visibility.getVisibilitySummary
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.visibility.VisibilityMoment
import com.docesforg.bura.visibility.VisibilityPeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.temporal.ChronoUnit

class GetVisibilitySummaryTest {
    private val period = VisibilityPeriod(
        listOf(
            VisibilityMoment(unixEpochStart, Visibility(1.0, Visibility.Unit.Meters)),
            VisibilityMoment(
                unixEpochStart.plus(1, ChronoUnit.HOURS),
                Visibility(2.0, Visibility.Unit.Meters)
            ),
            VisibilityMoment(
                unixEpochStart.plus(2, ChronoUnit.HOURS),
                Visibility(3.0, Visibility.Unit.Meters)
            )
        )
    )

    @Test
    fun `gets distance and description of now`() = runTest {
        val now = unixEpochStart.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        assertEquals(
            Visibility(2.0, Visibility.Unit.Meters),
            (getVisibilitySummary(now, period) as ForecastResult.Success).data.now
        )
    }

    @Test
    fun `summary is outdated when no now`() = runTest {
        val now = unixEpochStart.plus(3, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        assertEquals(ForecastResult.Outdated, getVisibilitySummary(now, period))
    }
}