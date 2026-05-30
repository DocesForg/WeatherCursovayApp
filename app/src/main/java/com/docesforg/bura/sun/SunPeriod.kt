package com.docesforg.bura.sun

import java.time.Duration
import java.time.LocalDateTime

class SunPeriod(val moments: List<SunMoment>) {
    init {
        requireNotEmpty()
        requireAscending()
    }

    fun momentsFrom(time: LocalDateTime, takeMomentsUpToHoursInFuture: Int? = null): List<SunMoment>? =
        moments.filter {
            val durationBetween = Duration.between(time, it.time)
            val hoursBetween = durationBetween.toHours()
            val maxHours = takeMomentsUpToHoursInFuture ?: Int.MAX_VALUE
            durationBetween >= Duration.ZERO && hoursBetween in 0..maxHours
        }.takeIf { it.isNotEmpty() }

    private fun requireNotEmpty() =
        require(moments.isNotEmpty()) { "Moments of SunPeriod must not be empty." }

    private fun requireAscending() {
        if (moments.size == 1) return
        var previousMoment = moments[0]
        for (i in 1..moments.lastIndex) {
            val nextMoment = moments[i]
            require(previousMoment.time < nextMoment.time) {
                "Moments of SunPeriod must be sorted and unique, but contained ${previousMoment.time} and ${nextMoment.time}."
            }
            previousMoment = nextMoment
        }
    }
}