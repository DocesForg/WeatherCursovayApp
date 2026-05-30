package com.docesforg.bura.units

import android.content.SharedPreferences
import androidx.core.content.edit
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.wind.WindSpeed

private const val TEMP_KEY = "temp_unit"
private const val RAIN_KEY = "rain_unit"
private const val SHOWERS_KEY = "showers_unit"
private const val SNOW_KEY = "snow_unit"
private const val PRECIP_KEY = "precip_unit"
private const val PRESSURE_KEY = "pressure_unit"
private const val VIS_KEY = "vis_unit"
private const val WIND_KEY = "wind_unit"
private val DefaultUnits = Units.Default

class SelectedUnitsRepository(private val prefs: SharedPreferences) {
    suspend fun getSelectedUnits(): Units =
        Units(
            temperature = prefs.getString(TEMP_KEY, null)?.let(Temperature.Unit::valueOf)
                ?: DefaultUnits.temperature,
            rain = prefs.getString(RAIN_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.rain,
            showers = prefs.getString(SHOWERS_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.showers,
            snow = prefs.getString(SNOW_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.snow,
            precipitation = prefs.getString(PRECIP_KEY, null)?.let(Precipitation.Unit::valueOf)
                ?: DefaultUnits.precipitation,
            windSpeed = prefs.getString(WIND_KEY, null)?.let(WindSpeed.Unit::valueOf)
                ?: DefaultUnits.windSpeed,
            pressure = prefs.getString(PRESSURE_KEY, null)?.let(Pressure.Unit::valueOf)
                ?: DefaultUnits.pressure,
            visibility = prefs.getString(VIS_KEY, null)?.let(Visibility.Unit::valueOf)
                ?: DefaultUnits.visibility
        )

    suspend fun selectRainUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(RAIN_KEY, unit.name) }

    suspend fun selectShowersUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(SHOWERS_KEY, unit.name) }

    suspend fun selectSnowUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(SNOW_KEY, unit.name) }

    suspend fun selectMixedPrecipitationUnit(unit: Precipitation.Unit) =
        prefs.edit { putString(PRECIP_KEY, unit.name) }

    suspend fun selectTemperatureUnit(unit: Temperature.Unit) =
        prefs.edit { putString(TEMP_KEY, unit.name) }

    suspend fun selectPressureUnit(unit: Pressure.Unit) =
        prefs.edit { putString(PRESSURE_KEY, unit.name) }

    suspend fun selectVisibilityUnit(unit: Visibility.Unit) =
        prefs.edit { putString(VIS_KEY, unit.name) }

    suspend fun selectWindSpeedUnit(unit: WindSpeed.Unit) =
        prefs.edit { putString(WIND_KEY, unit.name) }
}