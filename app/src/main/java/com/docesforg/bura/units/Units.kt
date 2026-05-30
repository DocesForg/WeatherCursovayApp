package com.docesforg.bura.units

import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.wind.WindSpeed

data class Units(
    val temperature: Temperature.Unit,
    val rain: Precipitation.Unit,
    val showers: Precipitation.Unit,
    val snow: Precipitation.Unit,
    val precipitation: Precipitation.Unit,
    val windSpeed: WindSpeed.Unit,
    val pressure: Pressure.Unit,
    val visibility: Visibility.Unit
) {
    companion object {
        val Default get() = Units(
            temperature = Temperature.Unit.DegreesCelsius,
            rain = Precipitation.Unit.Millimeters,
            showers = Precipitation.Unit.Millimeters,
            snow = Precipitation.Unit.Centimeters,
            precipitation = Precipitation.Unit.Millimeters,
            windSpeed = WindSpeed.Unit.MetersPerSecond,
            pressure = Pressure.Unit.Hectopascal,
            visibility = Visibility.Unit.Kilometers
        )
    }
}