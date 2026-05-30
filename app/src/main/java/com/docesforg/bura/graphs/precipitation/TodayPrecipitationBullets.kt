package com.docesforg.bura.graphs.precipitation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.graphs.common.GraphScreenSectionLabel
import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.Precipitation
import com.docesforg.bura.precipitation.Rain
import com.docesforg.bura.precipitation.Showers
import com.docesforg.bura.precipitation.Snow
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.rememberNumberFormat
import java.time.LocalDate

@Composable
fun TodayPrecipitationBullets(state: PrecipitationTotal.Today, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PrecipitationHours(past = true, state = state.past, modifier = Modifier.fillMaxWidth())
        PrecipitationHours(past = false, state = state.future, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun PrecipitationHours(
    past: Boolean,
    state: TotalPrecipitationInHours,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        GraphScreenSectionLabel(
            text = stringResource(
                if (past) R.string.cond_screen_precip_value_in_last_hours
                else R.string.cond_screen_precip_value_in_next_hours,
                rememberNumberFormat().format(state.hours)
            ),
        )
        PrecipitationBullets(state = state.total, modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
private fun PrecipitationTodayPreview() {
    AppTheme {
        Surface {
            TodayPrecipitationBullets(
                state = PrecipitationTotal.Today(
                    day = LocalDate.parse("1970-01-01"),
                    past = TotalPrecipitationInHours(
                        hours = 24,
                        total = MixedPrecipitation(
                            Rain(1.0, Precipitation.Unit.Millimeters),
                            snow = Snow(
                                70.0,
                                Precipitation.Unit.Millimeters
                            ).convertTo(Precipitation.Unit.Centimeters),
                            showers = Showers.ZeroMillimeters,
                            unit = Precipitation.Unit.Millimeters
                        )
                    ),
                    future = TotalPrecipitationInHours(
                        hours = 24,
                        MixedPrecipitation(
                            Rain(1.0, Precipitation.Unit.Millimeters),
                            snow = Snow(
                                70.0,
                                Precipitation.Unit.Millimeters
                            ).convertTo(Precipitation.Unit.Centimeters),
                            showers = Showers(11.0, Precipitation.Unit.Millimeters),
                            unit = Precipitation.Unit.Millimeters
                        )
                    )
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}