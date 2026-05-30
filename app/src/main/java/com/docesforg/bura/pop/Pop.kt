package com.docesforg.bura.pop

import java.util.Locale
import java.util.Objects

class Pop(val value: Double) : Comparable<Pop> {
    override fun compareTo(other: Pop): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean =
        other is Pop && other.value == value

    override fun hashCode(): Int = Objects.hash(value)

    override fun toString(): String = "${String.format(Locale.ROOT, "%.2f", value)}%"
}