package com.docesforg.bura.gust

import com.docesforg.bura.forecast.HourPeriod

class GustPeriod(moments: List<GustMoment>) : HourPeriod<GustMoment>(moments) {
    val maximum get() = maxOf { it.speed }
}