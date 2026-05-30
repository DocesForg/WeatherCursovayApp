package com.docesforg.bura.place.selected

import android.content.SharedPreferences
import androidx.core.content.edit
import com.docesforg.bura.auth.AuthSessionRepository
import com.docesforg.bura.place.Coordinates
import com.docesforg.bura.place.Location
import com.docesforg.bura.place.Place
import org.json.JSONObject
import java.time.ZoneId

private const val SELECTED_PLACE_KEY_PREFIX = "selected_place_json_"

class SelectedPlaceRepository(
    private val prefs: SharedPreferences,
    private val authSessionRepository: AuthSessionRepository,
) {
    suspend fun selectPlace(place: Place) {
        prefs.edit { putString(selectedPlaceKey(), placeToJson(place)) }
    }

    suspend fun getSelectedPlace(): Place? {
        val raw = prefs.getString(selectedPlaceKey(), null) ?: return null
        return runCatching { jsonToPlace(raw) }.getOrNull()
    }

    private fun selectedPlaceKey(): String {
        val accountScope = authSessionRepository.accountId()?.toString() ?: "guest"
        return "$SELECTED_PLACE_KEY_PREFIX$accountScope"
    }

    private fun placeToJson(place: Place): String = JSONObject().apply {
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

    private fun jsonToPlace(value: String): Place {
        val json = JSONObject(value)
        return Place(
            name = json.getString("name"),
            admin1 = json.optString("admin1").takeUnless { it.isBlank() || it == "null" },
            admin2 = json.optString("admin2").takeUnless { it.isBlank() || it == "null" },
            admin3 = json.optString("admin3").takeUnless { it.isBlank() || it == "null" },
            admin4 = json.optString("admin4").takeUnless { it.isBlank() || it == "null" },
            countryCode = json.getString("countryCode"),
            countryName = json.optString("countryName").takeUnless { it.isBlank() || it == "null" },
            location = Location(
                timeZone = ZoneId.of(json.getString("timeZone")),
                coordinates = Coordinates(
                    latitude = json.getDouble("latitude"),
                    longitude = json.getDouble("longitude")
                )
            )
        )
    }
}
