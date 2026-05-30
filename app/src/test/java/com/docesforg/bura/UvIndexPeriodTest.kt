package com.docesforg.bura

import com.docesforg.bura.uvindex.SunProtectionWindow
import com.docesforg.bura.uvindex.UvIndex
import com.docesforg.bura.uvindex.UvIndexMoment
import com.docesforg.bura.uvindex.UvIndexPeriod
import org.junit.Assert.*
import org.junit.Test
import java.time.temporal.ChronoUnit

private val dangerous = UvIndex(3)
private val safe = UvIndex(2)

class UvIndexPeriodTest {
    @Test
    fun `minimum and maximum`() {
        val firstMoment = unixEpochStart
        val secondMoment = firstMoment.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstMoment, UvIndex(0)),
                UvIndexMoment(secondMoment, UvIndex(1)),
            ),
        )
        assertEquals(UvIndex(0), period.minimum)
        assertEquals(UvIndex(1), period.maximum)
    }

    @Test
    fun `protection window with one dangerous hour`() {
        val firstDanger = unixEpochStart
        val firstSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(firstSafe, safe)
            ),
        )
        assertEquals(
            listOf(SunProtectionWindow(firstDanger, firstSafe)),
            period.protectionWindows
        )
    }

    @Test
    fun `protection window with multiple dangerous hours`() {
        val firstDanger = unixEpochStart
        val secondDanger = firstDanger.plus(1, ChronoUnit.HOURS)
        val firstSafe = secondDanger.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(firstSafe, safe)
            ),
        )
        assertEquals(
            listOf(SunProtectionWindow(firstDanger, firstSafe)),
            period.protectionWindows
        )
    }

    @Test
    fun `protection window when dangerous period has no end`() {
        val firstDanger = unixEpochStart
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstDanger,dangerous)))
        assertEquals(
            listOf(SunProtectionWindow(firstDanger, null)),
            period.protectionWindows
        )
    }

    @Test
    fun `no protection windows are empty when no dangerous hours`() {
        val firstSafe = unixEpochStart
        val period = UvIndexPeriod(listOf(UvIndexMoment(firstSafe, safe)))
        assertEquals(
            emptyList<SunProtectionWindow>(),
            period.protectionWindows
        )
    }

    @Test
    fun `multiple protection windows`() {
        val firstDanger = unixEpochStart
        val firstSafe = firstDanger.plus(1, ChronoUnit.HOURS)
        val secondDanger = firstSafe.plus(1, ChronoUnit.HOURS)
        val secondSafe = secondDanger.plus(1, ChronoUnit.HOURS)
        val thirdDanger = secondSafe.plus(1, ChronoUnit.HOURS)
        val period = UvIndexPeriod(
            moments = listOf(
                UvIndexMoment(firstDanger, dangerous),
                UvIndexMoment(firstSafe, safe),
                UvIndexMoment(secondDanger, dangerous),
                UvIndexMoment(secondSafe, safe),
                UvIndexMoment(thirdDanger, dangerous)
            ),
        )
        assertEquals(
            listOf(
                SunProtectionWindow(firstDanger, firstSafe),
                SunProtectionWindow(secondDanger, secondSafe),
                SunProtectionWindow(thirdDanger, null)
            ),
            period.protectionWindows
        )
    }
}