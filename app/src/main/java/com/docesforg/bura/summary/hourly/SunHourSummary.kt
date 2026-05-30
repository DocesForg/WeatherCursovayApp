package com.docesforg.bura.summary.hourly

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.sun.SunEvent
import com.docesforg.bura.common.rememberDateTimeHourMinuteFormatter
import java.time.LocalDateTime

@Composable
fun SunHourSummary(state: HourSummary.Sun, modifier: Modifier = Modifier) {
    val formatter = rememberDateTimeHourMinuteFormatter()
    HourSummary(
        time = { Text(state.time.format(formatter)) },
        icon = {
            Image(
                painter = painterResource(id = if (state.event == SunEvent.Sunrise) AppTheme.icons.sunrise else AppTheme.icons.sunset),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        pop = null,
        temperature = { Text(stringResource(if (state.event == SunEvent.Sunrise) R.string.sunrise_short else R.string.sunset_short)) },
        modifier = modifier
    )
}

@Preview
@Composable
private fun SunHourSummaryPreview() {
    AppTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SunHourSummary(
                state = HourSummary.Sun(
                    time = LocalDateTime.parse("2023-01-01T06:23"),
                    event = SunEvent.Sunrise
                )
            )
            SunHourSummary(
                state = HourSummary.Sun(
                    time = LocalDateTime.parse("2023-01-01T17:10"),
                    event = SunEvent.Sunset
                )
            )
        }
    }
}