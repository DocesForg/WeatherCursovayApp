package com.docesforg.bura.forecast

import com.docesforg.bura.gust.GustMoment
import com.docesforg.bura.gust.GustPeriod
import com.docesforg.bura.humidity.HumidityMoment
import com.docesforg.bura.humidity.HumidityPeriod
import com.docesforg.bura.pop.PopMoment
import com.docesforg.bura.pop.PopPeriod
import com.docesforg.bura.precipitation.PrecipitationMoment
import com.docesforg.bura.precipitation.PrecipitationPeriod
import com.docesforg.bura.pressure.PressureMoment
import com.docesforg.bura.pressure.PressurePeriod
import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.sun.SunMoment
import com.docesforg.bura.sun.SunPeriod
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import com.docesforg.bura.units.Units
import com.docesforg.bura.uvindex.UvIndexMoment
import com.docesforg.bura.uvindex.UvIndexPeriod
import com.docesforg.bura.visibility.VisibilityMoment
import com.docesforg.bura.visibility.VisibilityPeriod
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.wind.Wind
import com.docesforg.bura.wind.WindMoment
import com.docesforg.bura.wind.WindPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ForecastConverter {
    suspend fun fromData(data: ForecastData, toUnits: Units): Forecast =
        withContext(Dispatchers.Default) {
            val temperatureMoments = mutableListOf<TemperatureMoment>()
            val feelsLikeMoments = mutableListOf<TemperatureMoment>()
            val dewPointMoments = mutableListOf<TemperatureMoment>()
            val popMoments = mutableListOf<PopMoment>()
            val precipMoments = mutableListOf<PrecipitationMoment>()
            val uvIndexMoments = mutableListOf<UvIndexMoment>()
            val windMoments = mutableListOf<WindMoment>()
            val gustMoments = mutableListOf<GustMoment>()
            val pressureMoments = mutableListOf<PressureMoment>()
            val visibilityMoments = mutableListOf<VisibilityMoment>()
            val humidityMoments = mutableListOf<HumidityMoment>()
            val conditionMoments = mutableListOf<ConditionMoment>()

            for (i in data.times.indices) {
                val time = data.times[i]
                temperatureMoments.add(TemperatureMoment(time, data.temperature[i].convertTo(toUnits.temperature)))
                feelsLikeMoments.add(TemperatureMoment(time, data.feelsLikeTemperature[i].convertTo(toUnits.temperature)))
                dewPointMoments.add(TemperatureMoment(time, data.dewPointTemperature[i].convertTo(toUnits.temperature)))
                popMoments.add(PopMoment(time, data.pop[i]))
                val rain = data.rain[i].convertTo(toUnits.rain)
                val showers = data.showers[i].convertTo(toUnits.showers)
                val snowfall = data.snow[i].convertTo(toUnits.snow)
                precipMoments.add(PrecipitationMoment(time, MixedPrecipitation(rain, showers, snowfall, Precipitation.Unit.Millimeters).convertTo(toUnits.precipitation)))
                uvIndexMoments.add(UvIndexMoment(time, data.uvIndex[i]))
                windMoments.add(WindMoment(time, Wind(data.windSpeed[i].convertTo(toUnits.windSpeed), data.windDirection[i])))
                gustMoments.add(GustMoment(time, data.gustSpeed[i].convertTo(toUnits.windSpeed)))
                pressureMoments.add(PressureMoment(time, data.pressure[i].convertTo(toUnits.pressure)))
                visibilityMoments.add(VisibilityMoment(time, data.visibility[i].convertTo(toUnits.visibility)))
                humidityMoments.add(HumidityMoment(time, data.humidity[i]))
                conditionMoments.add(ConditionMoment(time, Condition(data.wmoCode[i], data.isDay[i])))
            }

            val temperature = TemperaturePeriod(temperatureMoments)
            val feelsLike = TemperaturePeriod(feelsLikeMoments)
            val dewPoint = TemperaturePeriod(dewPointMoments)
            val pop = PopPeriod(popMoments)
            val precipitation = PrecipitationPeriod(precipMoments)
            val uvIndex = UvIndexPeriod(uvIndexMoments)
            val wind = WindPeriod(windMoments)
            val gust = GustPeriod(gustMoments)
            val pressure = PressurePeriod(pressureMoments)
            val visibility = VisibilityPeriod(visibilityMoments)
            val humidity = HumidityPeriod(humidityMoments)
            val weatherDescription = ConditionPeriod(conditionMoments)
            val sun = createSunPeriod(data.sunrises, data.sunsets)

            return@withContext Forecast(
                temperature = temperature,
                feelsLike = feelsLike,
                dewPoint = dewPoint,
                sun = sun,
                pop = pop,
                precipitation = precipitation,
                uvIndex = uvIndex,
                wind = wind,
                gust = gust,
                pressure = pressure,
                visibility = visibility,
                humidity = humidity,
                condition = weatherDescription
            )
        }
}

fun createSunPeriod(
    sunrises: List<LocalDateTime>,
    sunsets: List<LocalDateTime>,
): SunPeriod? {
    val sortedSunMoments = mutableListOf<SunMoment>()
    for (i in sunrises.indices) {
        val sunrise = SunMoment(sunrises[i], SunEvent.Sunrise)
        val sunset = SunMoment(sunsets[i], SunEvent.Sunset)
        // https://github.com/docesforg/bura/issues/97#issuecomment-3001628460
        val isPolarNight = sunrise.time == sunset.time
        val isPolarDay = ChronoUnit.HOURS.between(sunrise.time, sunset.time) == 24L
        if (isPolarNight || isPolarDay) {
            continue
        } else if (sunset.time < sunrise.time) {
            sortedSunMoments.add(sunset)
            sortedSunMoments.add(sunrise)
        } else {
            sortedSunMoments.add(sunrise)
            sortedSunMoments.add(sunset)
        }
    }
    return sortedSunMoments.takeIf { it.isNotEmpty() }?.let { SunPeriod(it) }
}