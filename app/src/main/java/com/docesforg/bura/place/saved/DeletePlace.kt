package com.docesforg.bura.place.saved

import com.docesforg.bura.forecast.ForecastDataCacher
import com.docesforg.bura.place.Place

class DeletePlace(
    private val savedPlacesRepository: SavedPlacesRepository,
    private val forecastDataCacher: ForecastDataCacher
) {
    suspend operator fun invoke(place: Place) {
        savedPlacesRepository.deletePlace(place)
        forecastDataCacher.delete(place.location.coordinates)
    }
}