package com.docesforg.bura.place.saved

import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastRepository
import com.docesforg.bura.forecast.UpdatePolicy
import com.docesforg.bura.place.Place
import com.docesforg.bura.place.selected.SelectedPlaceRepository
import com.docesforg.bura.temperature.TemperaturePeriod
import com.docesforg.bura.units.SelectedUnitsRepository
import java.time.Instant
import java.time.LocalDateTime

class GetSavedPlaces(
    private val selectedUnitsRepo: SelectedUnitsRepository,
    private val selectedPlaceRepo: SelectedPlaceRepository,
    private val savedPlacesRepo: SavedPlacesRepository,
    private val forecastRepo: ForecastRepository,
) {
    suspend operator fun invoke(now: Instant): List<SavedPlace> {
        val selectedUnits = selectedUnitsRepo.getSelectedUnits()
        val selectedPlace = selectedPlaceRepo.getSelectedPlace()
        return savedPlacesRepo.getSavedPlaces().map { place ->
            val forecast = forecastRepo.forecast(
                coords = place.location.coordinates,
                units = selectedUnits,
                updatePolicy = UpdatePolicy.Eager
            )
            getSavedPlace(
                now = now,
                place = place,
                selected = place == selectedPlace,
                tempPeriod = forecast?.temperature,
                condPeriod = forecast?.condition,
            )
        }
    }
}

fun getSavedPlace(
    now: Instant,
    place: Place,
    selected: Boolean,
    tempPeriod: TemperaturePeriod?,
    condPeriod: ConditionPeriod?
): SavedPlace {
    val location = place.location
    val dateTimeAtPlace = now.atZone(place.location.timeZone).toLocalDateTime()
    val dateAtPlace = dateTimeAtPlace.toLocalDate()
    val tempDayAtPlace = tempPeriod?.getDay(dateAtPlace)
    val condDayAtPlace = condPeriod?.getDay(dateAtPlace)
    val conditions = if (tempDayAtPlace != null && condDayAtPlace != null) getConditions(
        dateTimeAtPlace,
        tempDayAtPlace,
        condDayAtPlace
    ) else null
    return SavedPlace(
        place = place,
        time = now.atZone(location.timeZone).toLocalTime(),
        selected = selected,
        conditions = conditions
    )
}

private fun getConditions(
    now: LocalDateTime,
    tempDay: TemperaturePeriod,
    conditionDay: ConditionPeriod
): SavedPlace.Conditions = SavedPlace.Conditions(
    temp = tempDay[now]!!.temperature,
    minTemp = tempDay.minimum,
    maxTemp = tempDay.maximum,
    condition = conditionDay[now]?.condition
        ?: conditionDay.day ?: conditionDay.night!!
)
