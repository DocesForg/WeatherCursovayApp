package com.docesforg.bura.place.search

import com.docesforg.bura.common.UserAgentProvider
import com.docesforg.bura.place.Coordinates
import com.docesforg.bura.place.Location
import com.docesforg.bura.place.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.time.ZoneId
import javax.net.ssl.HttpsURLConnection

class SearchPlaces(private val userAgentProvider: UserAgentProvider) {
    suspend operator fun invoke(query: String, languageCode: String): List<Place>? {
        val jsonString = downloadPlacesJson(query, languageCode) ?: return null
        val json = JSONObject(jsonString)
        val results = try {
            json.getJSONArray("results")
        } catch (_: Exception) {
            return emptyList()
        }
        val places = mutableListOf<Place>()
        withContext(Dispatchers.Default) {
            for (i in 0 until results.length()) {
                val currResult = results.getJSONObject(i)
                val countryCode = currResult.getStringOrNull("country_code") ?: continue
                val timeZone = currResult.getStringOrNull("timezone")?.let(ZoneId::of) ?: continue
                places.add(
                    Place(
                        name = currResult.getString("name"),
                        countryName = currResult.getStringOrNull("country"),
                        countryCode = countryCode,
                        admin1 = currResult.getStringOrNull("admin1"),
                        admin2 = currResult.getStringOrNull("admin2"),
                        admin3 = currResult.getStringOrNull("admin3"),
                        admin4 = currResult.getStringOrNull("admin4"),
                        location = Location(
                            timeZone = timeZone,
                            coordinates = Coordinates(
                                latitude = currResult.getDouble("latitude"),
                                longitude = currResult.getDouble("longitude")
                            )
                        )
                    )
                )
            }
        }
        return places
    }

    private suspend fun downloadPlacesJson(query: String, languageCode: String): String? = withContext(Dispatchers.IO) {
        val url = URL(openMeteoUrl(query, languageCode))
        val conn = try {
            url.openConnection() as HttpsURLConnection
        } catch (_: Exception) {
            return@withContext null
        }
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("User-Agent", userAgentProvider.userAgent)
            if (conn.responseCode != 200) return@withContext null
            BufferedReader(InputStreamReader(conn.inputStream)).use(BufferedReader::readText)
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun openMeteoUrl(query: String, languageCode: String): String =
        "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=$query" +
                "&count=100" +
                "&language=$languageCode" +
                "&format=json"

    private fun JSONObject.getStringOrNull(name: String): String? =
        try {
            getString(name)
        } catch (_: Exception) {
            null
        }
}