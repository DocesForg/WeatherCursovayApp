package com.docesforg.bura

import com.docesforg.bura.common.AppColors
import com.docesforg.bura.temperature.Temperature
import org.junit.Test

class TemperatureColorsTest {
    @Test
    fun `returns temperature colors for normal range`() {
        val min = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(20.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }

    @Test
    fun `maxes out at last temperature color`() {
        val min = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(100.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }

    @Test
    fun `mins out at first temperature color`() {
        val min = Temperature(-100.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(0.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }

    @Test
    fun `maxes and mins out at first and last temperature color`() {
        val min = Temperature(-100.0, Temperature.Unit.DegreesCelsius)
        val max = Temperature(100.0, Temperature.Unit.DegreesCelsius)
        val colors = AppColors.ForLightTheme
        assert(colors.temperatureColors(min, max).isNotEmpty())
    }
}