package com.docesforg.bura

import com.docesforg.bura.forecast.HourMoment
import org.junit.Test
import java.time.temporal.ChronoUnit

class HourMomentTest {
    @Test(expected = IllegalArgumentException::class)
    fun `hour must be truncated to hour`() {
        HourMoment(unixEpochStart.plus(1, ChronoUnit.MINUTES))
    }
}