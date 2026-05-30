package com.docesforg.bura.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.docesforg.bura.App
import com.docesforg.bura.forecast.ForecastRepository
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.graphs.pop.PopGraph
import com.docesforg.bura.graphs.pop.getPopGraphs
import com.docesforg.bura.graphs.precipitation.PrecipitationTotal
import com.docesforg.bura.graphs.precipitation.PrecipitationGraphs
import com.docesforg.bura.graphs.precipitation.getPrecipitationGraphs
import com.docesforg.bura.graphs.precipitation.getPrecipitationTotals
import com.docesforg.bura.graphs.temperature.TemperatureGraphSummary
import com.docesforg.bura.graphs.temperature.TemperatureGraphs
import com.docesforg.bura.graphs.temperature.getTemperatureGraphSummaries
import com.docesforg.bura.graphs.temperature.getTemperatureGraphs
import com.docesforg.bura.place.selected.SelectedPlaceRepository
import com.docesforg.bura.units.SelectedUnitsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class EssentialGraphsViewModel(
    private val placeRepo: SelectedPlaceRepository,
    private val unitsRepo: SelectedUnitsRepository,
    private val forecastRepo: ForecastRepository
) : ViewModel() {
    private val _state = MutableStateFlow<EssentialGraphsState>(EssentialGraphsState.Loading)
    val state = _state.asStateFlow()

    fun getGraphs() {
        viewModelScope.launch {
            if (_state.value !is EssentialGraphsState.Success) {
                _state.value = EssentialGraphsState.Loading
            }
            _state.value = getState()
        }
    }

    private suspend fun getState(): EssentialGraphsState {
        val location = placeRepo.getSelectedPlace()?.location ?: return EssentialGraphsState.NoSelectedPlace
        val coords = location.coordinates
        val units = unitsRepo.getSelectedUnits()
        val now = Instant.now().atZone(location.timeZone).toLocalDateTime()
        val forecast = forecastRepo.forecast(coords, units) ?: return EssentialGraphsState.FailedToDownload

        val tempGraphSummaries = getTemperatureGraphSummaries(now, tempPeriod = forecast.temperature, feelsPeriod = forecast.feelsLike, forecast.condition)
        when (tempGraphSummaries) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val tempGraphs = getTemperatureGraphs(now, forecast.temperature, forecast.condition)
        when (tempGraphs) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val popGraphs = getPopGraphs(now, forecast.pop, forecast.condition)
        when (popGraphs) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val precipGraphs = getPrecipitationGraphs(now, forecast.precipitation, forecast.condition)
        when (precipGraphs) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        val precipTotals = getPrecipitationTotals(now, forecast.precipitation)
        when (precipTotals) {
            ForecastResult.FailedToDownload -> return EssentialGraphsState.FailedToDownload
            ForecastResult.Outdated -> return EssentialGraphsState.Outdated
            is ForecastResult.Success -> Unit
        }

        return EssentialGraphsState.Success(
            tempGraphSummaries = tempGraphSummaries.data,
            tempGraphs = tempGraphs.data,
            popGraphs = popGraphs.data,
            precipGraphs = precipGraphs.data,
            precipTotals = precipTotals.data
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return EssentialGraphsViewModel(
                    container.selectedPlaceRepo,
                    container.selectedUnitsRepo,
                    container.forecastRepo,
                ) as T
            }
        }
    }
}

sealed interface EssentialGraphsState {
    data class Success(
        val tempGraphSummaries: List<TemperatureGraphSummary>,
        val tempGraphs: TemperatureGraphs,
        val popGraphs: List<PopGraph>,
        val precipGraphs: PrecipitationGraphs,
        val precipTotals: List<PrecipitationTotal>
    ) : EssentialGraphsState

    data object Loading : EssentialGraphsState
    data object FailedToDownload : EssentialGraphsState
    data object Outdated : EssentialGraphsState
    data object NoSelectedPlace : EssentialGraphsState
}