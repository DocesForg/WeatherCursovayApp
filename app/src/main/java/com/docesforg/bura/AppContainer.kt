package com.docesforg.bura

import android.content.Context
import android.content.SharedPreferences
import com.docesforg.bura.account.AccountRepository
import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.common.UserAgentProvider
import com.docesforg.bura.forecast.ForecastConverter
import com.docesforg.bura.forecast.ForecastDataCacher
import com.docesforg.bura.forecast.ForecastDataDownloader
import com.docesforg.bura.forecast.ForecastRepository
import com.docesforg.bura.place.saved.AddPlaceToFavorites
import com.docesforg.bura.place.saved.DeletePlace
import com.docesforg.bura.place.saved.GetSavedPlaces
import com.docesforg.bura.place.saved.SavedPlacesRepository
import com.docesforg.bura.place.search.SearchPlaces
import com.docesforg.bura.place.selected.SelectedPlaceRepository
import com.docesforg.bura.place.selected.SelectPlace
import com.docesforg.bura.platform.local.BuraDatabase
import com.docesforg.bura.platform.remote.ApiProvider
import com.docesforg.bura.radio.RadioSignalRepository
import com.docesforg.bura.support.SupportRepository
import com.docesforg.bura.units.SelectedUnitsRepository
import com.docesforg.bura.place.saved.FavoritesSyncRepository

class AppContainer(private val appContext: Context) {
    val prefs: SharedPreferences get() = appContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val authSessionRepository by lazy { AuthSessionRepository(prefs) }
    private val root get() = appContext.filesDir
    private val userAgentProvider get() = UserAgentProvider(appContext)

    private val forecastCacher by lazy { ForecastDataCacher(root) }
    val forecastRepo by lazy {
        ForecastRepository(
            cacher = forecastCacher,
            downloader = ForecastDataDownloader(userAgentProvider),
            converter = ForecastConverter()
        )
    }

    // Open-Meteo API remains unchanged; this backend client is for account/support/radio features.
    private val apiProvider by lazy {
        ApiProvider(
            baseUrl = "http://10.0.2.2:8080/",
            authSessionRepository = authSessionRepository,
            onUnauthorized = authSessionRepository::clearSession,
        )
    }
    val backendApi get() = apiProvider.backendApi

    private val roomDb by lazy { BuraDatabase.create(appContext) }
    val localDao get() = roomDb.dao()

    val selectedPlaceRepo by lazy { SelectedPlaceRepository(prefs, authSessionRepository) }
    val selectedUnitsRepo by lazy { SelectedUnitsRepository(prefs) }

    private val savedPlacesRepo by lazy { SavedPlacesRepository(root, backendApi, authSessionRepository) }
    val accountRepository by lazy { AccountRepository(backendApi, localDao, authSessionRepository, savedPlacesRepo) }
    val favoritesSyncRepository by lazy { FavoritesSyncRepository(backendApi, localDao, authSessionRepository) }
    val supportRepository by lazy { SupportRepository(backendApi, localDao, authSessionRepository) }
    val radioSignalRepository by lazy { RadioSignalRepository(backendApi, localDao, authSessionRepository) }

    val getSavedPlaces get() = GetSavedPlaces(selectedUnitsRepo, selectedPlaceRepo, savedPlacesRepo, forecastRepo)
    val searchPlaces get() = SearchPlaces(userAgentProvider)
    val selectPlace get() = SelectPlace(selectedPlaceRepo)
    val addPlaceToFavorites get() = AddPlaceToFavorites(savedPlacesRepo)
    val deletePlace get() = DeletePlace(savedPlacesRepo, forecastCacher)
}
