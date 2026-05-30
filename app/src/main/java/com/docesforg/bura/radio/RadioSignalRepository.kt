package com.docesforg.bura.radio

import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.platform.local.BuraDao
import com.docesforg.bura.platform.local.RadioSignalTestEntity
import com.docesforg.bura.platform.remote.BuraBackendApi
import com.docesforg.bura.platform.remote.RadioSignalRequestDto

class RadioSignalRepository(
    private val api: BuraBackendApi,
    private val dao: BuraDao,
    private val authSessionRepository: AuthSessionRepository,
) {
    suspend fun run(
        cityA: String,
        cityB: String,
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double,
        frequencyMhz: Double = 900.0,
    ): RadioSignalTestEntity {
        val accountId = authSessionRepository.accountId() ?: error("Account id is missing in session")
        val response = api.runSignalTest(
            accountId = accountId,
            body = RadioSignalRequestDto(
                cityA = cityA,
                cityB = cityB,
                latitudeA = latitudeA,
                longitudeA = longitudeA,
                latitudeB = latitudeB,
                longitudeB = longitudeB,
                frequencyMhz = frequencyMhz,
            )
        )
        val entity = RadioSignalTestEntity(
            id = response.id,
            accountId = accountId,
            cityA = response.cityA,
            cityB = response.cityB,
            distanceKm = response.distanceKm,
            pathLossDb = response.pathLossDb,
            quality = response.quality,
            latencyMs = response.latencyMs,
            speedMbps = response.speedMbps,
            createdAt = response.createdAt,
        )
        dao.upsertRadioTest(entity)
        return entity
    }

    suspend fun history(): List<RadioSignalTestEntity> {
        val accountId = authSessionRepository.accountId() ?: return emptyList()
        val remote = runCatching { api.radioHistory(accountId) }.getOrElse { return dao.getRadioTests(accountId) }
        val entities = remote.map {
            RadioSignalTestEntity(
                id = it.id,
                accountId = accountId,
                cityA = it.cityA,
                cityB = it.cityB,
                distanceKm = it.distanceKm,
                pathLossDb = it.pathLossDb,
                quality = it.quality,
                latencyMs = it.latencyMs,
                speedMbps = it.speedMbps,
                createdAt = it.createdAt,
            )
        }
        entities.forEach { dao.upsertRadioTest(it) }
        return entities
    }
}
