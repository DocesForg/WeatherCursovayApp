package com.docesforg.bura.graphs.common

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Objects

class GraphTime(
    val value: LocalTime,
    val meta: Meta
) {
    constructor(
        hour: LocalDateTime,
        now: LocalDateTime
    ) : this(value = hour.toLocalTime(), meta = getMeta(hour, now))

    enum class Meta {
        Past, Present, Future
    }

    override fun equals(other: Any?): Boolean =
        other is GraphTime && other.value == value && other.meta == meta

    override fun hashCode(): Int = Objects.hash(value, meta)
}

private fun getMeta(hour: LocalDateTime, now: LocalDateTime): GraphTime.Meta {
    val nowTrunc = now.truncatedTo(ChronoUnit.HOURS)
    return when {
        hour < nowTrunc -> GraphTime.Meta.Past
        hour == nowTrunc -> GraphTime.Meta.Present
        else -> GraphTime.Meta.Future
    }
}