package com.docesforg.bura.forecast

import com.docesforg.bura.pop.Pop
import com.docesforg.bura.humidity.Humidity
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.uvindex.UvIndex
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.wind.WindDirection
import com.docesforg.bura.wind.WindSpeed
import java.time.Instant
import java.time.LocalDateTime

class ForecastData(
    val timestamp: Instant,
    val times: List<LocalDateTime>,
    val temperature: List<Temperature>,
    val feelsLikeTemperature: List<Temperature>,
    val dewPointTemperature: List<Temperature>,
    val sunrises: List<LocalDateTime>,
    val sunsets: List<LocalDateTime>,
    val pop: List<Pop>,
    val rain: List<Rain>,
    val showers: List<Showers>,
    val snow: List<Snow>,
    val uvIndex: List<UvIndex>,
    val windSpeed: List<WindSpeed>,
    val windDirection: List<WindDirection>,
    val gustSpeed: List<WindSpeed>,
    val pressure: List<Pressure>,
    val visibility: List<Visibility>,
    val humidity: List<Humidity>,
    val wmoCode: List<Int>,
    val isDay: List<Boolean>,
)