package com.docesforg.bura.place.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.docesforg.bura.App
import com.docesforg.bura.place.Place
import com.docesforg.bura.place.saved.AddPlaceToFavorites
import com.docesforg.bura.place.saved.DeletePlace
import com.docesforg.bura.place.saved.SavedPlace
import com.docesforg.bura.place.saved.GetSavedPlaces
import com.docesforg.bura.place.search.SearchPlaces
import com.docesforg.bura.place.selected.SelectPlace
import com.docesforg.bura.place.selected.SelectedPlaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class PlacePickerViewModel(
    private val selectedPlaceRepo: SelectedPlaceRepository,
    private val selectPlace: SelectPlace,
    private val getSavedPlaces: GetSavedPlaces,
    private val searchPlaces: SearchPlaces,
    private val addPlaceToFavorites: AddPlaceToFavorites,
    private val deletePlace: DeletePlace
) : ViewModel() {
    private val _state = MutableStateFlow(
        PlacePickerState(
            loading = false,
            selectedPlace = null,
            results = PlacePickerResults.Initial,
            isSelectedPlaceFavorite = false,
        )
    )
    val state get() = _state.asStateFlow()

    fun getSelectedPlace() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val place = selectedPlaceRepo.getSelectedPlace()
            _state.value = _state.value.copy(
                loading = false,
                selectedPlace = place,
                isSelectedPlaceFavorite = place?.let { isFavorite(it) } ?: false,
            )
        }
    }

    fun selectPlace(place: Place) {
        viewModelScope.launch {
            selectPlace.invoke(place)
            _state.value = _state.value.copy(
                loading = false,
                selectedPlace = place,
                isSelectedPlaceFavorite = isFavorite(place),
            )
        }
    }

    fun getSavedPlaces() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                results = PlacePickerResults.SavedPlaces(getSavedPlaces.invoke(Instant.now())),
                loading = false,
                isSelectedPlaceFavorite = _state.value.selectedPlace?.let { isFavorite(it) } ?: false,
            )
        }
    }

    fun searchPlaces(query: String, languageCode: String) {
        val trimmedQuery = query.trim()
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val results = searchPlaces.invoke(trimmedQuery, languageCode)
            _state.value = _state.value.copy(
                loading = false,
                results = PlacePickerResults.SearchedPlaces(trimmedQuery, results)
            )
        }
    }

    fun toggleSelectedPlaceFavorite(onDone: (added: Boolean) -> Unit = {}) {
        val selected = _state.value.selectedPlace ?: return
        viewModelScope.launch {
            val alreadyFavorite = isFavorite(selected)
            if (alreadyFavorite) {
                deletePlace.invoke(selected)
            } else {
                addPlaceToFavorites.invoke(selected)
            }
            _state.value = _state.value.copy(isSelectedPlaceFavorite = !alreadyFavorite)
            if (_state.value.results is PlacePickerResults.SavedPlaces) {
                _state.value = _state.value.copy(
                    results = PlacePickerResults.SavedPlaces(getSavedPlaces.invoke(Instant.now()))
                )
            }
            onDone(!alreadyFavorite)
        }
    }

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            deletePlace.invoke(place)
            _state.value = _state.value.copy(
                loading = false,
                results = PlacePickerResults.SavedPlaces(getSavedPlaces.invoke(Instant.now())),
                selectedPlace = selectedPlaceRepo.getSelectedPlace(),
                isSelectedPlaceFavorite = _state.value.selectedPlace?.let { isFavorite(it) } ?: false,
            )
        }
    }

    private suspend fun isFavorite(place: Place): Boolean {
        return getSavedPlaces.invoke(Instant.now()).any { it.place.location.coordinates == place.location.coordinates }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val container = (checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as App).container
                return PlacePickerViewModel(
                    container.selectedPlaceRepo,
                    container.selectPlace,
                    container.getSavedPlaces,
                    container.searchPlaces,
                    container.addPlaceToFavorites,
                    container.deletePlace
                ) as T
            }
        }
    }
}

data class PlacePickerState(
    val loading: Boolean,
    val selectedPlace: Place?,
    val results: PlacePickerResults,
    val isSelectedPlaceFavorite: Boolean,
)

sealed interface PlacePickerResults {
    data object Initial : PlacePickerResults
    data class SavedPlaces(val places: List<SavedPlace>) : PlacePickerResults
    data class SearchedPlaces(val query: String, val places: List<Place>?) : PlacePickerResults
}
