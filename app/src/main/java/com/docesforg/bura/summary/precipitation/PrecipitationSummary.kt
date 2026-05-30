package com.docesforg.bura.summary.precipitation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import com.docesforg.bura.precipitation.string
import com.docesforg.bura.precipitation.typeString
import com.docesforg.bura.precipitation.unitString
import com.docesforg.bura.precipitation.valueString
import com.docesforg.bura.summary.SummaryTile
import com.docesforg.bura.summary.ValueAndUnit
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.rememberDateTimeFormatter
import com.docesforg.bura.common.rememberNumberFormat
import java.time.LocalDate

@Composable
fun PrecipitationSummary(
    state: PrecipitationSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SummaryTile(
        label = { Label(past = state.past) },
        value = { Value(past = state.past) },
        supportingValue = {
            Text(
                stringResource(
                    R.string.precip_value_in_last_hours,
                    rememberNumberFormat().format(state.past.inHours)
                )
            )
        },
        bottom = { Bottom(future = state.future) },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun Label(past: PastPrecipitation) {
    Text(past.total.typeString())
}

@Composable
private fun Value(past: PastPrecipitation) {
    if (past.total.unit == Precipitation.Unit.Inches) {
        Text(text = past.total.string())
    } else {
        ValueAndUnit(
            value = past.total.valueString(),
            unit = past.total.unitString()
        )
    }
}

@Composable
fun Bottom(future: FuturePrecipitation) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    Text(
        text = when (future) {
            is FuturePrecipitation.InHours -> stringResource(
                when (future.total) {
                    is MixedPrecipitation -> R.string.precip_value_mixed_in_next_hours
                    is Rain -> R.string.precip_value_rain_in_next_hours
                    is Showers -> R.string.precip_value_showers_in_next_hours
                    is Snow -> R.string.precip_value_snow_in_next_hours
                },
                future.total.string(),
                rememberNumberFormat().format(future.inHours)
            )

            is FuturePrecipitation.OnDay -> stringResource(
                when (future.total) {
                    is MixedPrecipitation -> R.string.precip_value_mixed_on_day
                    is Rain -> R.string.precip_value_rain_on_day
                    is Showers -> R.string.precip_value_showers_on_day
                    is Snow -> R.string.precip_value_snow_on_day
                },
                future.total.string(),
                formatter.format(future.onDay)
            )

            is FuturePrecipitation.None -> stringResource (
                R.string.precip_value_none_in_next_days,
                rememberNumberFormat().format(future.inDays)
            )
        }
    )
}

@Preview
@Composable
private fun PrecipitationSummaryPreview() {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .width(200.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrecipitationSummary(
                    state = PrecipitationSummary(
                        past = PastPrecipitation(
                            inHours = 12,
                            total = Rain(12.59, Precipitation.Unit.Millimeters)
                        ),
                        future = FuturePrecipitation.OnDay(
                            onDay = LocalDate.parse("2023-01-01"),
                            total = Rain(5.0, Precipitation.Unit.Millimeters)
                        )
                    ),
                    onClick = {},
                    modifier = Modifier.aspectRatio(1f)
                )
                PrecipitationSummary(
                    state = PrecipitationSummary(
                        past = PastPrecipitation(
                            inHours = 24,
                            total = Rain.ZeroMillimeters
                        ),
                        future = FuturePrecipitation.InHours(
                            inHours = 24,
                            total = Rain.ZeroMillimeters
                        ),
                    ),
                    onClick = {},
                    modifier = Modifier.aspectRatio(1f)
                )
                PrecipitationSummary(
                    state = PrecipitationSummary(
                        past = PastPrecipitation(
                            inHours = 24,
                            total = Snow(100.0, Precipitation.Unit.Millimeters).convertTo(Precipitation.Unit.Inches)
                        ),
                        future = FuturePrecipitation.None(inDays = 7)
                    ),
                    onClick = {},
                    modifier = Modifier.aspectRatio(1f)
                )
            }
        }
    }
}