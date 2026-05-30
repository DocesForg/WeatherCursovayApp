package com.docesforg.bura.place.saved

import com.docesforg.bura.place.Place

class AddPlaceToFavorites(
    private val savedPlacesRepository: SavedPlacesRepository,
) {
    suspend operator fun invoke(place: Place) {
        savedPlacesRepository.savePlace(place)
    }
}
