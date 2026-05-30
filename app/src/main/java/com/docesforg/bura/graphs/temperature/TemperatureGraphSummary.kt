package com.docesforg.bura.graphs.temperature

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.HighLowText
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.image
import com.docesforg.bura.condition.string
import com.docesforg.bura.summary.now.NowSummary
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.string
import java.time.LocalDate

@Composable
fun TemperatureGraphSummary(state: TemperatureGraphSummary, modifier: Modifier = Modifier) {
    val now = state.now
    NowSummary(
        temperature = {
            when (now) {
                null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.maxTemp.string())
                        Text(
                            text = state.minTemp.string(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> Text(text = now.temp.string())
            }
        },
        icon = {
            Image(
                painter = state.condition.image(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        highLow = {
            when (now) {
                null -> when {
                    state.minTemp.unit == Temperature.Unit.DegreesCelsius -> Text(stringResource(R.string.cond_screen_temp_unit_celsius))
                    else -> Text(stringResource(R.string.cond_screen_temp_unit_fahrenheit))
                }

                else -> HighLowText(
                    high = state.maxTemp.string(),
                    low = state.minTemp.string()
                )
            }
        },
        feelsLike = {
            if (now != null) Text(
                stringResource(
                    R.string.feels_like_value,
                    now.feelsLike.string()
                )
            )
        },
        condition = { if (now != null) Text(state.condition.string()) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun TemperatureGraphSummaryTodayPreview() {
    AppTheme {
        Surface {
            TemperatureGraphSummary(
                state = TemperatureGraphSummary(
                    day = LocalDate.parse("1970-01-03"),
                    minTemp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    maxTemp = Temperature(30.0, Temperature.Unit.DegreesCelsius),
                    condition = Condition(wmoCode = 53, isDay = true),
                    now = null
                ),
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TemperatureGraphSummaryDayPreview() {
    AppTheme {
        Surface {
            TemperatureGraphSummary(
                state = TemperatureGraphSummary(
                    day = LocalDate.parse("1970-01-03"),
                    minTemp = Temperature(10.0, Temperature.Unit.DegreesCelsius),
                    maxTemp = Temperature(30.0, Temperature.Unit.DegreesCelsius),
                    condition = Condition(wmoCode = 53, isDay = true),
                    now = null,
                ),
                modifier = Modifier
                    .width(400.dp)
                    .padding(16.dp)
            )
        }
    }
}