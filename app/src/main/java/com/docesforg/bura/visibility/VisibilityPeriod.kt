package com.docesforg.bura.visibility

import com.docesforg.bura.forecast.HourPeriod

class VisibilityPeriod(moments: List<VisibilityMoment>) : HourPeriod<VisibilityMoment>(moments) {
    val minimum get() = minOf { it.visibility }

    val maximum get() = maxOf { it.visibility }
}