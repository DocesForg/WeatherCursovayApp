package com.docesforg.bura.summary.hourly

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.rememberDateTimeHourFormatter
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.image
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.string
import com.docesforg.bura.summary.PopAndDrop
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.string
import java.time.LocalDateTime

@Composable
fun WeatherHourSummary(state: HourSummary.Weather, modifier: Modifier = Modifier) {
    val formatter = rememberDateTimeHourFormatter()
    HourSummary(
        time = { Text(if (state.isNow) stringResource(R.string.date_time_now) else state.time.format(formatter)) },
        icon = {
            Image(
                painter = state.desc.image(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        pop = state.pop?.string()?.let {
            @Composable {
                PopAndDrop(it)
            }
        },
        temperature = { Text(state.temp.string()) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun WeatherSummaryPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeatherHourSummary(
                state = HourSummary.Weather(
                    time = LocalDateTime.parse("2023-01-01T14:00"),
                    isNow = true,
                    temp = Temperature(20.0, Temperature.Unit.DegreesCelsius),
                    pop = Pop(50.0),
                    desc = Condition(wmoCode = 51, isDay = true)
                ),
            )
            WeatherHourSummary(
                state = HourSummary.Weather(
                    time = LocalDateTime.parse("2023-01-01T15:00"),
                    isNow = false,
                    temp = Temperature(20.0, Temperature.Unit.DegreesCelsius),
                    pop = null,
                    desc = Condition(wmoCode = 1, isDay = true)
                )
            )
        }
    }
}