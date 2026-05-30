package com.docesforg.bura.wind

import java.util.Objects

class Wind(
    val speed: WindSpeed,
    val from: WindDirection
) {
    val to: WindDirection = WindDirection(degrees = from.degrees + 180)

    override fun equals(other: Any?): Boolean =
        other is Wind && other.speed == speed && other.from == from

    override fun hashCode(): Int = Objects.hash(speed, from)

    override fun toString(): String = "$speed from $from"
}