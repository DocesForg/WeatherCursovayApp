package com.docesforg.bura

import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.sun.SunMoment
import com.docesforg.bura.sun.SunPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

class SunMomentListTest {
    @Test
    fun `splits into future moments`() {
        val startOfTime = unixEpochStart
        val firstSunset = startOfTime.plus(1, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeFirstSunset = firstSunset.minus(15, ChronoUnit.MINUTES)
        val firstSunrise = startOfTime.plus(3, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeFirstSunrise = firstSunrise.minus(15, ChronoUnit.MINUTES)
        val secondSunset = startOfTime.plus(5, ChronoUnit.HOURS).plus(10, ChronoUnit.MINUTES)
        val beforeSecondSunset = secondSunset.minus(15, ChronoUnit.MINUTES)
        val afterSecondSunset = secondSunset.plus(15, ChronoUnit.MINUTES)
        val period = SunPeriod(
            moments = listOf(
                SunMoment(firstSunset, SunEvent.Sunset),
                SunMoment(firstSunrise, SunEvent.Sunrise),
                SunMoment(secondSunset, SunEvent.Sunset)
            )
        )
        val threeMoments = period.momentsFrom(beforeFirstSunset)
        assertEquals(3, threeMoments!!.size)
        assertTrue(threeMoments[0].event == SunEvent.Sunset)
        assertTrue(threeMoments[0].time == firstSunset)
        assertTrue(threeMoments[1].event == SunEvent.Sunrise)
        assertTrue(threeMoments[1].time == firstSunrise)

        val twoMoments = period.momentsFrom(beforeFirstSunrise)
        assertEquals(2, twoMoments!!.size)
        assertTrue(twoMoments[0].event == SunEvent.Sunrise)
        assertTrue(twoMoments[0].time == firstSunrise)
        assertTrue(twoMoments[1].event == SunEvent.Sunset)
        assertTrue(twoMoments[1].time == secondSunset)

        val oneMoment = period.momentsFrom(beforeSecondSunset)
        assertEquals(1, oneMoment!!.size)
        assertTrue(oneMoment[0].event == SunEvent.Sunset)
        assertTrue(oneMoment[0].time == secondSunset)

        assertNull(period.momentsFrom(afterSecondSunset))
    }
}