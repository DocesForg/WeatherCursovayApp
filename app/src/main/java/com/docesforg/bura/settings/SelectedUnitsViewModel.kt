package com.docesforg.bura.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.docesforg.bura.App
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.pressure.Pressure
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.units.SelectedUnitsRepository
import com.docesforg.bura.units.Units
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.wind.WindSpeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectedUnitsViewModel(private val repo: SelectedUnitsRepository) : ViewModel() {
    private val _state = MutableStateFlow<Units?>(null)
    val state: StateFlow<Units?> = _state.asStateFlow()

    fun getSettings() {
        viewModelScope.launch {
            _state.value = repo.getSelectedUnits()
        }
    }

    fun selectTemperatureUnit(value: Temperature.Unit) {
        viewModelScope.launch {
            repo.selectTemperatureUnit(value)
            _state.value = _state.value?.copy(temperature = value)
        }
    }

    fun selectWindUnit(value: WindSpeed.Unit) {
        viewModelScope.launch {
            repo.selectWindSpeedUnit(value)
            _state.value = _state.value?.copy(windSpeed = value)
        }
    }

    fun selectPrecipitationUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectMixedPrecipitationUnit(value)
            _state.value = _state.value?.copy(precipitation = value)
        }
    }

    fun selectRainUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectRainUnit(value)
            _state.value = _state.value?.copy(rain = value)
        }
    }

    fun selectShowersUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectShowersUnit(value)
            _state.value = _state.value?.copy(showers = value)
        }
    }

    fun selectSnowUnit(value: Precipitation.Unit) {
        viewModelScope.launch {
            repo.selectSnowUnit(value)
            _state.value = _state.value?.copy(snow = value)
        }
    }

    fun selectPressureUnit(value: Pressure.Unit) {
        viewModelScope.launch {
            repo.selectPressureUnit(value)
            _state.value = _state.value?.copy(pressure = value)
        }
    }

    fun selectVisibilityUnit(value: Visibility.Unit) {
        viewModelScope.launch {
            repo.selectVisibilityUnit(value)
            _state.value = _state.value?.copy(visibility = value)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return SelectedUnitsViewModel(container.selectedUnitsRepo) as T
            }
        }
    }
}