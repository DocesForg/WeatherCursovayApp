package com.docesforg.bura.graphs.common

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

// Source: Graphics Gems 1 by Andrew S. Glassner and https://stackoverflow.com/a/16363437
class NiceScale(
    min: Double,
    max: Double,
    maxTicks: Int
) {
    val niceMin: Double
    val niceMax: Double
    val niceSpacing: Double
    val niceSteps: List<Double>

    init {
        val range = niceNum(srcNum = max - min, round = false)
        niceSpacing = niceNum(srcNum = range / (maxTicks - 1), round = true)
        niceMin = floor(min / niceSpacing) * niceSpacing
        niceMax = ceil(max / niceSpacing) * niceSpacing
        val scale = mutableListOf(niceMin)
        do {
            val tick = scale.last() + niceSpacing
            scale.add(tick)
        } while (tick < niceMax)
        niceSteps = scale
    }

    private fun niceNum(srcNum: Double, round: Boolean): Double {
        val exponent: Double = floor(log10(srcNum))
        val fraction = srcNum / 10.0.pow(exponent)
        val niceFraction: Double = if (round) {
            when {
                fraction < 1.5 -> 1.0
                fraction < 3 -> 2.0
                fraction < 7.0 -> 5.0
                else -> 10.0
            }
        } else {
            when {
                fraction <= 1 -> 1.0
                fraction <= 2 -> 2.0
                fraction <= 5 -> 5.0
                else -> 10.0
            }
        }
        return niceFraction * 10.0.pow(exponent)
    }
}