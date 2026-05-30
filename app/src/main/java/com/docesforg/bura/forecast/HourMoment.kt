package com.docesforg.bura.forecast
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

open class HourMoment(val hour: LocalDateTime) {
    init {
        require(hour == hour.truncatedTo(ChronoUnit.HOURS)) {
            "Time of HourMoment must be whole hour, but was $hour."
        }
    }
}