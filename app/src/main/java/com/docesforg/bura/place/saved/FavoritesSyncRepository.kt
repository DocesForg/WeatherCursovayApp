package com.docesforg.bura.place.saved

import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.platform.local.BuraDao
import com.docesforg.bura.platform.local.FavoriteCityEntity
import com.docesforg.bura.platform.remote.BuraBackendApi

class FavoritesSyncRepository(
    private val api: BuraBackendApi,
    private val dao: BuraDao,
    private val authSessionRepository: AuthSessionRepository,
) {
    suspend fun sync(): List<FavoriteCityEntity> {
        val accountId = authSessionRepository.accountId() ?: error("Account id is missing in session")
        val items = api.favorites(accountId).map {
            FavoriteCityEntity(
                id = it.id,
                accountId = accountId,
                cityName = it.cityName,
                latitude = it.latitude,
                longitude = it.longitude,
            )
        }
        dao.upsertFavorites(items)
        return dao.getFavorites(accountId)
    }
}
