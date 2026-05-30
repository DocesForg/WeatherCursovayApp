package com.docesforg.bura.humidity

import com.docesforg.bura.forecast.HourPeriod

class HumidityPeriod(moments: List<HumidityMoment>) : HourPeriod<HumidityMoment>(moments) {
    val average get() = map { it.humidity }.reduce { acc, percent -> acc + percent } / size
}