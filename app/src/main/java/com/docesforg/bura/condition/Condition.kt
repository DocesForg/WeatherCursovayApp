package com.docesforg.bura.condition

import java.util.Objects

class Condition(
    val wmoCode: Int,
    val isDay: Boolean
) {
    override fun equals(other: Any?): Boolean =
        other is Condition && other.wmoCode == wmoCode && other.isDay == isDay

    override fun hashCode(): Int = Objects.hash(wmoCode, isDay)

    override fun toString(): String = "$wmoCode${if (isDay) "d" else "n"}"
}