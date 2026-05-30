package com.docesforg.bura.wind

import java.util.Locale
import java.util.Objects
import kotlin.math.ceil

class WindDirection(degrees: Double) {
    val degrees: Double = degrees + ceil(-degrees / 360) * 360
    val compass: Compass = Compass.entries[(degrees / 22.5 + 0.5).toInt() % 16]

    enum class Compass {
        N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW
    }

    override fun equals(other: Any?): Boolean =
        other is WindDirection && other.degrees == degrees

    override fun hashCode(): Int = Objects.hash(degrees)

    override fun toString(): String = "${String.format(Locale.ROOT, "%.2f", degrees)}° ($compass)"
}