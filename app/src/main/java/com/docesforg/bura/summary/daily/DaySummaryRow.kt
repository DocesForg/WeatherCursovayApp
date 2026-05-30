package com.docesforg.bura.summary.daily

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R
import com.docesforg.bura.common.AppTheme
import com.docesforg.bura.common.capitalize
import com.docesforg.bura.common.rememberAppLocale
import com.docesforg.bura.common.rememberDateTimeFormatter
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.image
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.string
import com.docesforg.bura.summary.PopAndDrop
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.string
import java.time.LocalDate

private val roundedRadius = 12.dp
private val squareRadius = 4.dp
private val verticalPadding = 8.dp

private val firstShape = RoundedCornerShape(
    topStart = roundedRadius,
    topEnd = roundedRadius,
    bottomStart = squareRadius,
    bottomEnd = squareRadius
)

private val lastShape = RoundedCornerShape(
    topStart = squareRadius,
    topEnd = squareRadius,
    bottomStart = roundedRadius,
    bottomEnd = roundedRadius
)

private val middleShape = RoundedCornerShape(size = squareRadius)

enum class DaySummaryPosition {
    First, Middle, Last;

    fun shape() = when (this) {
        First -> firstShape
        Middle -> middleShape
        Last -> lastShape
    }
}

@Composable
fun DaySummaryRow(
    state: DaySummary,
    position: DaySummaryPosition,
    absMin: Temperature,
    absMax: Temperature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatter = rememberDateTimeFormatter(ofPattern = R.string.date_time_pattern_dow)
    Surface(
        shape = remember(position) { position.shape() },
        tonalElevation = 1.dp,
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = verticalPadding, horizontal = 16.dp)
        ) {
            DayAndPopMaxHeightDummy()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f)
            ) {
                DayAndPop(
                    day = {
                        Text(
                            text = if (state.isToday) stringResource(R.string.date_time_today) else state.time.format(formatter).capitalize(rememberAppLocale()),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    pop = state.pop?.string()?.let {
                        @Composable {
                            PopAndDrop(it)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = state.desc.image(),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(2f)
            ) {
                CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                    val maxTempWidth = rememberMaxTempWidth()
                    Text(
                        text = state.min.string(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.width(maxTempWidth)
                    )
                    AppleTemperatureScale(
                        absMin = absMin,
                        absMax = absMax,
                        min = state.min,
                        now = state.tempNow,
                        max = state.max,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = state.max.string(),
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.width(maxTempWidth)
                    )
                }
            }
        }
    }
}

@Composable
fun DaySummaryRowSkeleton(
    color: State<Color>,
    position: DaySummaryPosition,
    modifier: Modifier = Modifier
) {
    Box(modifier.background(color = color.value, shape = position.shape())) {
        DayAndPopMaxHeightDummy(modifier = Modifier.padding(vertical = verticalPadding))
    }
}

@Composable
private fun DayAndPop(
    day: @Composable () -> Unit,
    pop: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
            content = day
        )
        pop?.let { it() }
    }
}

@Composable
private fun DayAndPopMaxHeightDummy(modifier: Modifier = Modifier) {
    DayAndPop(
        day = { Text("") },
        pop = { PopAndDrop("") },
        modifier = modifier
            .width(0.dp)
            .alpha(0f)
    )
}

@Composable
private fun rememberMaxTempWidth(): Dp {
    val measurer = rememberTextMeasurer()
    val maxTemp = remember { Temperature(999.0, Temperature.Unit.DegreesCelsius) }
    val density = LocalDensity.current
    val maxTempString = maxTemp.string()
    val textStyle = LocalTextStyle.current
    return remember(measurer, density, maxTempString, textStyle) {
        with(density) {
            measurer.measure(maxTempString, textStyle).size.width.toDp()
        }
    }
}

@Preview
@Composable
private fun DaySummaryPreview() {
    AppTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            val absoluteMin = remember { Temperature(0.0, Temperature.Unit.DegreesCelsius) }
            val absoluteMax = remember { Temperature(20.0, Temperature.Unit.DegreesCelsius) }
            DaySummaryRow(
                absMin = absoluteMin,
                absMax = absoluteMax,
                state = DaySummary(
                    isToday = true,
                    time = LocalDate.parse("2023-01-01"),
                    tempNow = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                    min = Temperature(2.0, Temperature.Unit.DegreesCelsius),
                    max = Temperature(19.0, Temperature.Unit.DegreesCelsius),
                    pop = null,
                    desc = Condition(wmoCode = 1, isDay = true)
                ),
                position = DaySummaryPosition.First,
                onClick = {}
            )
            DaySummaryRow(
                absMin = absoluteMin,
                absMax = absoluteMax,
                state = DaySummary(
                    isToday = false,
                    time = LocalDate.parse("2023-01-02"),
                    tempNow = Temperature(5.0, Temperature.Unit.DegreesCelsius),
                    min = Temperature(0.0, Temperature.Unit.DegreesCelsius),
                    max = Temperature(5.0, Temperature.Unit.DegreesCelsius),
                    pop = Pop(15.0),
                    desc = Condition(wmoCode = 51, isDay = true)
                ),
                position = DaySummaryPosition.Middle,
                onClick = {}
            )
            DaySummaryRow(
                absMin = absoluteMin,
                absMax = absoluteMax,
                state = DaySummary(
                    isToday = false,
                    time = LocalDate.parse("2023-01-03"),
                    tempNow = Temperature(9.0, Temperature.Unit.DegreesCelsius),
                    min = Temperature(7.0, Temperature.Unit.DegreesCelsius),
                    max = Temperature(15.0, Temperature.Unit.DegreesCelsius),
                    pop = Pop(0.0),
                    desc = Condition(wmoCode = 2, isDay = true)
                ),
                position = DaySummaryPosition.Last,
                onClick = {}
            )
        }
    }
}