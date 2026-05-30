package com.docesforg.bura.common

import org.json.JSONArray
import org.json.JSONObject

fun <T : Any> Collection<T>.mapToJSONArray(transform: (T) -> Any = { it }) =
    JSONArray(map(transform))

fun <T> JSONArray.mapToList(transform: (String) -> T): List<T> {
    val result = mutableListOf<T>()
    for (i in 0 until length()) {
        result.add(transform(get(i).toString()))
    }
    return result
}

fun JSONObject.getStringOrNull(name: String): String? = if (isNull(name)) null else getString(name)