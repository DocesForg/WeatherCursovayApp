package com.docesforg.bura.sun

import java.time.LocalDateTime
import java.util.Objects

class SunMoment(
    val time: LocalDateTime,
    val event: SunEvent
) {
    override fun equals(other: Any?): Boolean =
        other is SunMoment && other.time == time && other.event == event

    override fun hashCode(): Int = Objects.hash(time, event)

    override fun toString(): String = "$time: $event"
}