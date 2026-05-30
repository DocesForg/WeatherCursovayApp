package com.docesforg.bura.place

data class Place(
    val name: String,
    val admin1: String?,
    val admin2: String?,
    val admin3: String?,
    val admin4: String?,
    val countryCode: String,
    val countryName: String?,
    val location: Location
)