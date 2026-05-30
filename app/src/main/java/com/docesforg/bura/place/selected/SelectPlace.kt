package com.docesforg.bura.place.selected

import com.docesforg.bura.place.Place

class SelectPlace(
    private val selectedPlaceRepository: SelectedPlaceRepository,
) {
    suspend operator fun invoke(place: Place) {
        selectedPlaceRepository.selectPlace(place)
    }
}
