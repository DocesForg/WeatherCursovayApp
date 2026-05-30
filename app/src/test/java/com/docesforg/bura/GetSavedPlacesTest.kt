package com.docesforg.bura

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.place.Coordinates
import com.docesforg.bura.place.Location
import com.docesforg.bura.place.Place
import com.docesforg.bura.place.saved.SavedPlace
import com.docesforg.bura.place.saved.getSavedPlace
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class GetSavedPlacesTest {
    @Test
    fun `gets saved place with conditions`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val momentDateTime = momentInstant.atZone(ZoneOffset.UTC).toLocalDateTime()
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val place = Place(
            name = "first", "", "", "", "", "", "",
            Location(ZoneId.of("GMT"), Coordinates(latitude = 0.0, longitude = 1.0))
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    hour = momentDateTime,
                    temperature = Temperature(10.0, Temperature.Unit.DegreesCelsius)
                )
            ),
        )
        val condPeriod = ConditionPeriod(
            listOf(
                ConditionMoment(
                    hour = momentDateTime,
                    condition = Condition(0, true)
                )
            ),
        )
        val result = getSavedPlace(now, place, false, tempPeriod, condPeriod)
        assertEquals(
            SavedPlace(
                place = place,
                time = LocalTime.parse("00:10"),
                selected = false,
                conditions = SavedPlace.Conditions(
                    temp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    minTemp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    maxTemp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    condition = Condition(0, true)
                )
            ),
            result
        )
    }

    @Test
    fun `gets saved place without conditions`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val place = Place(
            name = "second", "", "", "", "", "", "",
            Location(ZoneId.of("GMT+1"), Coordinates(latitude = 0.0, longitude = 10.0))
        )
        val tempPeriod = null
        val condPeriod = null
        val result = getSavedPlace(now, place, true, tempPeriod, condPeriod)
        assertEquals(
            SavedPlace(
                place = place,
                time = LocalTime.parse("01:10"),
                selected = true,
                conditions = null,
            ),
            result
        )
    }

    @Test
    fun `gets saved place without conditions when data is mixed`() = runTest {
        val momentInstant = Instant.ofEpochSecond(0)
        val momentDateTime = momentInstant.atZone(ZoneOffset.UTC).toLocalDateTime()
        val now = momentInstant.plus(10, ChronoUnit.MINUTES)
        val place = Place(
            name = "second", "", "", "", "", "", "",
            Location(ZoneId.of("GMT+1"), Coordinates(latitude = 0.0, longitude = 10.0))
        )
        val tempPeriod = TemperaturePeriod(
            listOf(
                TemperatureMoment(
                    hour = momentDateTime,
                    temperature = Temperature(10.0, Temperature.Unit.DegreesCelsius)
                )
            ),
        )
        val condPeriod = null
        val result = getSavedPlace(now, place, true, tempPeriod, condPeriod)
        assertEquals(
            SavedPlace(
                place = place,
                time = LocalTime.parse("01:10"),
                selected = true,
                conditions = null,
            ),
            result
        )
    }
}
