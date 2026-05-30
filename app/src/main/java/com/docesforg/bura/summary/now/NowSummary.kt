package com.docesforg.bura.summary.now

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.HighLowText
import com.docesforg.bura.common.TextSkeleton
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.image
import com.docesforg.bura.condition.string
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.string

@Composable
fun NowSummary(state: NowSummary, modifier: Modifier = Modifier) {
    NowSummary(
        date = { Text(stringResource(id = R.string.date_time_now)) },
        temperature = { Text(state.temp.string()) },
        icon = {
            Image(
                painter = state.cond.image(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        },
        highLow = {
            HighLowText(
                high = state.maxTemp.string(),
                low = state.minTemp.string()
            )
        },
        feelsLike = {
            Text(
                stringResource(
                    id = R.string.feels_like_value,
                    state.feelsLike.string()
                )
            )
        },
        condition = { Text(state.cond.string()) },
        modifier = modifier
    )
}

@Composable
fun NowSummary(
    temperature: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    highLow: @Composable () -> Unit,
    feelsLike: @Composable () -> Unit,
    condition: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    date: (@Composable () -> Unit)? = null,
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        Column {
            date?.let {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleMedium,
                    LocalContentColor provides MaterialTheme.colorScheme.secondary,
                    content = it
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.displayMedium,
                    content = temperature
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    icon()
                }
            }
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                content = highLow
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                content = condition
            )
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                content = feelsLike
            )
        }
    }
}

@Composable
fun NowSummarySkeleton(
    color: State<Color>,
    modifier: Modifier = Modifier,
    withDate: Boolean = false
) {
    NowSummary(
        date = if (withDate) {
            @Composable {
                TextSkeleton(
                    color = color,
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(vertical = 2.dp),
                    modifier = Modifier.width(64.dp)
                )
            }
        } else null,
        temperature = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(160.dp)
            )
        },
        icon = {},
        highLow = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(150.dp)
            )
        },
        feelsLike = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(80.dp)
            )
        },
        condition = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(64.dp)
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun NowSummaryPreview() {
    AppTheme(darkTheme = true) {
        Surface(modifier = Modifier.width(400.dp)) {
            NowSummary(
                state = NowSummary(
                    temp = Temperature(20.0, Temperature.Unit.DegreesCelsius),
                    feelsLike = Temperature(18.0, Temperature.Unit.DegreesCelsius),
                    minTemp = Temperature(15.0, Temperature.Unit.DegreesCelsius),
                    maxTemp = Temperature(25.0, Temperature.Unit.DegreesCelsius),
                    cond = Condition(
                        wmoCode = 53,
                        isDay = true
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}