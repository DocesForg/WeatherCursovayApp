package com.docesforg.bura.humidity

import java.util.Locale
import java.util.Objects

class Humidity(val value: Double) : Comparable<Humidity> {
    operator fun plus(other: Humidity) = Humidity(value + other.value)

    operator fun div(other: Int) = Humidity(value / other)

    override fun compareTo(other: Humidity): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        other is Humidity && other.value == value

    override fun hashCode(): Int = Objects.hash(value)

    override fun toString(): String = "${String.format(Locale.ROOT, "%.2f", value)}%"
}