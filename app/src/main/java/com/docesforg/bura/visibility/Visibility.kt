package com.docesforg.bura.visibility

import com.docesforg.bura.visibility.Visibility.Unit
import java.util.Locale
import java.util.Objects

class Visibility(
    val value: Double,
    val unit: Unit
) : Comparable<Visibility> {
    private val meters: Double = when (unit) {
        Unit.Meters -> value
        Unit.Feet -> value * 3.28084
        Unit.Kilometers -> value * 1000
        Unit.Miles -> value / 0.00062137
    }

    val description: Description = when {
        meters < 500 -> Description.VeryLow
        meters < 1000 -> Description.Low
        meters <= 6000 -> Description.Fair
        meters <= 10_000 -> Description.Clear
        else -> Description.Perfect
    }

    fun convertTo(unit: Unit): Visibility {
        var newUnit = unit
        var newValue = metersTo(meters, newUnit)

        if (newValue < 0.1 && unit == Unit.Kilometers) {
            newUnit = Unit.Meters
            newValue = metersTo(meters, newUnit)
        }

        if (newValue < 0.1 && unit == Unit.Miles) {
            newUnit = Unit.Feet
            newValue = metersTo(meters, newUnit)
        }

        return Visibility(
            value = newValue,
            unit = newUnit
        )
    }

    enum class Unit {
        Meters,
        Feet,
        Kilometers,
        Miles,
    }

    enum class Description {
        VeryLow,
        Low,
        Fair,
        Clear,
        Perfect
    }

    override fun compareTo(other: Visibility): Int = meters.compareTo(other.meters)

    override fun equals(other: Any?): Boolean =
        other is Visibility && other.meters == meters && other.value == value && other.unit == unit

    override fun hashCode(): Int = Objects.hash(meters, value, unit)

    override fun toString(): String {
        val suffix = when (unit) {
            Unit.Meters -> "m"
            Unit.Feet -> "ft"
            Unit.Kilometers -> "km"
            Unit.Miles -> "mi"
        }
        return "${String.format(Locale.ROOT, "%.2f", value)} $suffix ($description)"
    }
}

private fun metersTo(meters: Double, unit: Unit): Double = meters * when (unit) {
    Unit.Meters -> 1.0
    Unit.Feet -> 3.28084
    Unit.Kilometers -> 0.001
    Unit.Miles -> 0.000621371
}