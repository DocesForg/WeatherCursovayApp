package com.docesforg.bura.pressure

import java.util.Locale
import java.util.Objects

class Pressure(
    val value: Double,
    val unit: Unit
) : Comparable<Pressure> {
    private val hectopascal: Double = when (unit) {
        Unit.Hectopascal -> value
        Unit.InchesOfMercury -> value * 33.86389
        Unit.MillimetersOfMercury -> value * 1.33322
    }

    fun convertTo(unit: Unit): Pressure = Pressure(
        value = hectopascal * when (unit) {
            Unit.Hectopascal -> 1.0
            Unit.InchesOfMercury -> 0.02953
            Unit.MillimetersOfMercury -> 0.750062
        },
        unit = unit
    )

    operator fun plus(other: Pressure): Pressure {
        val sum = hectopascal + other.hectopascal
        return Pressure(
            value = sum,
            unit = Unit.Hectopascal
        ).convertTo(unit)
    }

    operator fun div(other: Int): Pressure {
        val result = hectopascal / other
        return Pressure(
            value = result,
            unit = Unit.Hectopascal
        ).convertTo(unit)
    }

    override fun compareTo(other: Pressure): Int =
        hectopascal.compareTo(other.hectopascal)

    override fun equals(other: Any?): Boolean =
        other is Pressure && other.hectopascal == hectopascal && other.value == value && other.unit == unit

    override fun hashCode(): Int = Objects.hash(hectopascal, value, unit)

    override fun toString(): String {
        val suffix = when (unit) {
            Unit.Hectopascal -> "hPa"
            Unit.InchesOfMercury -> "inHg"
            Unit.MillimetersOfMercury -> "mmHg"
        }
        return "${String.format(Locale.ROOT, "%.2f", value)} $suffix"
    }

    enum class Unit {
        Hectopascal,
        InchesOfMercury,
        MillimetersOfMercury
    }
}