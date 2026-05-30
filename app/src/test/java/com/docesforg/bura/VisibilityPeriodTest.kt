package com.docesforg.bura

import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.visibility.VisibilityMoment
import com.docesforg.bura.visibility.VisibilityPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class VisibilityPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = VisibilityPeriod(
            moments = listOf(
                VisibilityMoment(firstMoment, Visibility(1.0, Visibility.Unit.Meters)),
                VisibilityMoment(secondMoment, Visibility(2.0, Visibility.Unit.Meters))
            )
        )
        assertEquals(Visibility(1.0, Visibility.Unit.Meters), period.minimum)
        assertEquals(Visibility(2.0, Visibility.Unit.Meters), period.maximum)
    }
}