package com.docesforg.bura.place.saved

import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.common.getStringOrNull
import com.docesforg.bura.place.Coordinates
import com.docesforg.bura.place.Location
import com.docesforg.bura.place.Place
import com.docesforg.bura.platform.remote.BuraBackendApi
import com.docesforg.bura.platform.remote.FavoriteCityRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.ZoneId

class SavedPlacesRepository(
    private val root: File,
    private val api: BuraBackendApi,
    private val authSessionRepository: AuthSessionRepository,
) {
    private var memoryCache: MutableList<Place>? = null

    suspend fun savePlace(place: Place) {
        val accountId = authSessionRepository.accountId()
        if (accountId != null) {
            runCatching {
                api.addFavorite(
                    accountId = accountId,
                    body = FavoriteCityRequestDto(
                        cityName = place.name,
                        latitude = place.location.coordinates.latitude,
                        longitude = place.location.coordinates.longitude,
                    )
                )
            }
        }

        if (getSavedPlace(place.location.coordinates) != null) return

        val file = File(getDir(), place.location.coordinates.id)
        val json = convertPlaceToJson(place)
        withContext(Dispatchers.IO) { file.writeText(json) }
        memoryCache?.add(0, place)
    }

    suspend fun getSavedPlaces(): List<Place> {
        syncFromBackendIfPossible()

        val fromMemory = memoryCache
        if (fromMemory != null) return fromMemory

        val fromFiles = getDir()
            .listFiles()
            ?.sortedByDescending {
                val attrs = Files.readAttributes(it.toPath(), BasicFileAttributes::class.java)
                attrs.lastModifiedTime()
            }
            ?.map { convertFileToPlace(it) }
            ?: emptyList()
        memoryCache = mutableListOf<Place>().apply { addAll(fromFiles) }
        return fromFiles
    }

    suspend fun getSavedPlace(coords: Coordinates): Place? =
        getSavedPlaces().firstOrNull { it.location.coordinates == coords }

    suspend fun deletePlace(place: Place) {
        val accountId = authSessionRepository.accountId()
        if (accountId != null) {
            runCatching {
                val remote = api.favorites(accountId)
                val match = remote.firstOrNull {
                    it.latitude == place.location.coordinates.latitude && it.longitude == place.location.coordinates.longitude
                }
                if (match != null) {
                    api.deleteFavorite(accountId = accountId, favoriteId = match.id)
                }
            }
        }

        val file = findPlaceFile(place.location.coordinates) ?: return
        withContext(Dispatchers.IO) { file.delete() }
        memoryCache?.remove(place)
    }

    suspend fun deletePlacesForAccount(accountId: Long) {
        val accountDir = File(root, "places/$accountId")
        withContext(Dispatchers.IO) {
            accountDir.listFiles()?.forEach { it.delete() }
            accountDir.delete()
        }
        if (authSessionRepository.accountId() == accountId) {
            memoryCache = null
        }
    }

    private suspend fun syncFromBackendIfPossible() {
        val accountId = authSessionRepository.accountId() ?: return
        runCatching {
            val remote = api.favorites(accountId)
            val mapped = remote.map {
                Place(
                    name = it.cityName,
                    admin1 = null,
                    admin2 = null,
                    admin3 = null,
                    admin4 = null,
                    countryCode = "",
                    countryName = null,
                    location = Location(
                        timeZone = ZoneId.of("UTC"),
                        coordinates = Coordinates(it.latitude, it.longitude)
                    )
                )
            }
            replaceLocalCache(mapped)
        }
    }

    private suspend fun replaceLocalCache(places: List<Place>) {
        val dir = getDir()
        withContext(Dispatchers.IO) {
            dir.listFiles()?.forEach { it.delete() }
            places.forEach { place ->
                File(dir, place.location.coordinates.id).writeText(convertPlaceToJson(place))
            }
        }
        memoryCache = places.toMutableList()
    }

    private suspend fun convertFileToPlace(file: File): Place =
        withContext(Dispatchers.IO) {
            val jsonString = file.readText()
            val record = JSONObject(jsonString)
            Place(
                name = record.getString("name"),
                admin1 = record.getStringOrNull("admin1"),
                admin2 = record.getStringOrNull("admin2"),
                admin3 = record.getStringOrNull("admin3"),
                admin4 = record.getStringOrNull("admin4"),
                countryCode = record.getString("countryCode"),
                countryName = record.getStringOrNull("countryName"),
                location = Location(
                    timeZone = ZoneId.of(record.getString("timeZone")),
                    coordinates = Coordinates(
                        latitude = record.getDouble("latitude"),
                        longitude = record.getDouble("longitude")
                    ),
                )
            )
        }

    private suspend fun convertPlaceToJson(place: Place): String =
        withContext(Dispatchers.Default) {
            JSONObject().apply {
                put("name", place.name)
                put("admin1", place.admin1 ?: JSONObject.NULL)
                put("admin2", place.admin2 ?: JSONObject.NULL)
                put("admin3", place.admin3 ?: JSONObject.NULL)
                put("admin4", place.admin4 ?: JSONObject.NULL)
                put("countryCode", place.countryCode)
                put("countryName", place.countryName ?: JSONObject.NULL)
                put("timeZone", place.location.timeZone.id)
                put("latitude", place.location.coordinates.latitude)
                put("longitude", place.location.coordinates.longitude)
            }.toString()
        }

    private suspend fun findPlaceFile(coords: Coordinates): File? =
        withContext(Dispatchers.IO) {
            val allFiles = getDir().listFiles()
            val targetName = coords.id
            allFiles?.firstOrNull { it.name == targetName }
        }

    private suspend fun getDir(): File =
        withContext(Dispatchers.IO) {
            val accountSubDir = authSessionRepository.accountId()?.toString() ?: "guest"
            File(root, "places/$accountSubDir").apply { mkdirs() }
        }
}
