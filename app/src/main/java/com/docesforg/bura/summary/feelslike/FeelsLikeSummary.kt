package com.docesforg.bura.summary.feelslike

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.docesforg.bura.summary.SummaryTile
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.string

@Composable
fun FeelsLikeSummary(state: FeelsLikeSummary, modifier: Modifier = Modifier) {
    SummaryTile(
        label = { Text(stringResource(R.string.feels_like)) },
        supportingValue = {
            if (state.vsActual != FeelsVsActual.Similar) {
                Text(stringResource(R.string.feels_like_value_actual, state.actualNow.string()))
            }
        },
        value = { Text(state.feelsLikeNow.string()) },
        bottom = {
            Text(
                stringResource(
                    when (state.vsActual) {
                        FeelsVsActual.Colder -> R.string.feels_like_colder_than_actual
                        FeelsVsActual.Cooler -> R.string.feels_like_cooler_than_actual
                        FeelsVsActual.Similar -> R.string.feels_like_similar_to_actual
                        FeelsVsActual.Warmer -> R.string.feels_like_warmer_than_actual
                        FeelsVsActual.Hotter -> R.string.feels_like_hotter_than_actual
                    }
                )
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun FeelsLikeSummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeelsLikeSummary(
                state = FeelsLikeSummary(
                    feelsLikeNow = Temperature(20.0, Temperature.Unit.DegreesCelsius),
                    actualNow = Temperature(25.0, Temperature.Unit.DegreesCelsius),
                    vsActual = FeelsVsActual.Warmer
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}