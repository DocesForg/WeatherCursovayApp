package com.docesforg.bura.wind

import com.docesforg.bura.forecast.HourPeriod

class WindPeriod(moments: List<WindMoment>) : HourPeriod<WindMoment>(moments) {
    val minimumSpeed get() = minOf { it.wind.speed }

    val maximumSpeed get() = maxOf { it.wind.speed }
}