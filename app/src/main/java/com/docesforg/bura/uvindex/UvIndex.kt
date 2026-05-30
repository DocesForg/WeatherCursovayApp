package com.docesforg.bura.uvindex

import java.util.Locale
import java.util.Objects

class UvIndex(val value: Int) : Comparable<UvIndex> {
    val risk: Risk = when {
        value < 3 -> Risk.Low
        value < 5 -> Risk.Moderate
        value < 7 -> Risk.High
        value < 10 -> Risk.VeryHigh
        else -> Risk.Extreme
    }

    enum class Risk {
        Low,
        Moderate,
        High,
        VeryHigh,
        Extreme
    }

    override fun compareTo(other: UvIndex): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        other is UvIndex && other.value == value

    override fun hashCode(): Int = Objects.hash(value)

    override fun toString(): String = "${String.format(Locale.ROOT, "%.2f", value)} ($risk)"
}