package com.docesforg.bura.forecast

import com.docesforg.bura.gust.GustPeriod
import com.docesforg.bura.humidity.HumidityPeriod
import com.docesforg.bura.pop.PopPeriod
import com.docesforg.bura.precipitation.PrecipitationPeriod
import com.docesforg.bura.pressure.PressurePeriod
import com.docesforg.bura.sun.SunPeriod
import com.docesforg.bura.temperature.TemperaturePeriod
import com.docesforg.bura.uvindex.UvIndexPeriod
import com.docesforg.bura.visibility.VisibilityPeriod
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.wind.WindPeriod

data class Forecast(
    val temperature: TemperaturePeriod,
    val feelsLike: TemperaturePeriod,
    val dewPoint: TemperaturePeriod,
    val sun: SunPeriod?,
    val pop: PopPeriod,
    val precipitation: PrecipitationPeriod,
    val uvIndex: UvIndexPeriod,
    val wind: WindPeriod,
    val gust: GustPeriod,
    val pressure: PressurePeriod,
    val visibility: VisibilityPeriod,
    val humidity: HumidityPeriod,
    val condition: ConditionPeriod
) {
    init {
        requireMatching(
            temperature,
            feelsLike,
            dewPoint,
            pop,
            precipitation,
            uvIndex,
            wind,
            gust,
            pressure,
            visibility,
            humidity,
            condition
        )
    }
}