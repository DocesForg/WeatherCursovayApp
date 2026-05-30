package com.docesforg.bura.summary.sun

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.common.capitalize
import com.docesforg.bura.common.rememberAppLocale
import com.docesforg.bura.common.rememberDateTimeDayAndTimeFormatter
import com.docesforg.bura.summary.SummaryTile
import com.docesforg.bura.common.rememberDateTimeFormatter
import com.docesforg.bura.common.rememberDateTimeHourMinuteFormatter
import com.docesforg.bura.common.rememberNumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun SunSummary(state: SunSummary, modifier: Modifier = Modifier) {
    val locale = rememberAppLocale()
    val dayFormatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    val timeFormatter = rememberDateTimeHourMinuteFormatter()
    val dayAndTimeFormatter = rememberDateTimeDayAndTimeFormatter()

    SummaryTile(
        label = { Text(stringResource(if (state is Sunrise) R.string.sunrise else R.string.sunset)) },
        value = {
            Text(
                text = when (state) {
                    is Sunrise.WithSunsetSoon -> timeFormatter.format(state.time)
                    is Sunset.WithSunriseSoon -> timeFormatter.format(state.time)

                    is Sunrise.Later -> dayFormatter.format(state.time).capitalize(locale)
                    is Sunset.Later -> dayFormatter.format(state.time).capitalize(locale)

                    is Sunrise.WithSunsetLater -> timeFormatter.format(state.time)
                    is Sunset.WithSunriseLater -> timeFormatter.format(state.time)

                    is Sunrise.OutOfSight -> {
                        val numberFormat = rememberNumberFormat()
                        if (state.forDuration.toDays() < 1) stringResource(R.string.sunrise_value_more_than_hours_away, numberFormat.format(state.forDuration.toHours()))
                        else stringResource(R.string.sunrise_value_more_than_days_away, numberFormat.format(state.forDuration.toDays()))
                    }
                    is Sunset.OutOfSight -> {
                        val numberFormat = rememberNumberFormat()
                        if (state.forDuration.toDays() < 1) stringResource(R.string.sunset_value_more_than_hours_away, numberFormat.format(state.forDuration.toHours()))
                        else stringResource(R.string.sunset_value_more_than_days_away, numberFormat.format(state.forDuration.toDays()))
                    }
                }
            )
        },
        supportingValue = {
            when (state) {
                is Sunrise.Later -> Text(timeFormatter.format(state.time))
                is Sunset.Later -> Text(timeFormatter.format(state.time))
                else -> Unit
            }
        },
        bottom = {
            Text(
                text = when (state) {
                    is Sunrise.WithSunsetSoon -> stringResource(R.string.sunset_value, timeFormatter.format(state.sunset))
                    is Sunset.WithSunriseSoon -> stringResource(R.string.sunrise_value, timeFormatter.format(state.sunrise))

                    is Sunrise.WithSunsetLater -> stringResource(R.string.sunset_value, dayAndTimeFormatter.format(state.sunset))
                    is Sunset.WithSunriseLater -> stringResource(R.string.sunrise_value, dayAndTimeFormatter.format(state.sunrise))

                    is Sunrise.Later, is Sunrise.OutOfSight -> stringResource(R.string.sunrise_not_today)
                    is Sunset.Later, is Sunset.OutOfSight -> stringResource(R.string.sunset_not_today)
                }
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun SunSummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SunSummary(
                state = Sunrise.WithSunsetSoon(
                    time = LocalTime.of(6, 20),
                    sunset = LocalTime.of(18, 30)
                )
            )
            SunSummary(
                state = Sunrise.WithSunsetLater(
                    time = LocalTime.of(5, 20),
                    sunset = LocalDateTime.parse("2023-01-01T18:30")
                )
            )
            SunSummary(
                state = Sunrise.Later(
                    time = LocalDateTime.parse("2023-01-01T18:30"),
                )
            )
            SunSummary(
                state = Sunrise.OutOfSight(
                    forDuration = Duration.ofDays(1),
                )
            )
        }
    }
}