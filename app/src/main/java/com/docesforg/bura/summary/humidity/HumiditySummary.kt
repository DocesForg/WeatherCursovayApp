package com.docesforg.bura.summary.humidity

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
import com.docesforg.bura.humidity.Humidity
import com.docesforg.bura.humidity.string
import com.docesforg.bura.summary.SummaryTile
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.string

@Composable
fun HumiditySummary(state: HumiditySummary, modifier: Modifier = Modifier) {
    SummaryTile(
        label = { Text(stringResource(R.string.humidity)) },
        value = { Text(text = state.humidityNow.string()) },
        bottom = {
            Text(
                stringResource(
                    R.string.dew_point_value_right_now,
                    state.dewPointNow.string()
                )
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun HumiditySummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HumiditySummary(
                state = HumiditySummary(
                    humidityNow = Humidity(92.0),
                    dewPointNow = Temperature(19.0, Temperature.Unit.DegreesCelsius)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}