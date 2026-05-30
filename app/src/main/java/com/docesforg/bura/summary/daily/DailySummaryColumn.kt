package com.docesforg.bura.summary.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DailySummaryColumn(
    state: DailySummary,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        state.days.forEachIndexed { index, day ->
            DaySummaryRow(
                state = day,
                absMin = state.minTemp,
                absMax = state.maxTemp,
                modifier = Modifier.fillMaxWidth(),
                position = when (index) {
                    0 -> DaySummaryPosition.First
                    state.days.lastIndex -> DaySummaryPosition.Last
                    else -> DaySummaryPosition.Middle
                },
                onClick = { onDayClick(day.time) }
            )
        }
    }
}

@Composable
fun DailySummaryColumnSkeleton(
    color: State<Color>,
    modifier: Modifier = Modifier,
    rows: Int = 7,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        repeat(rows) {
            DaySummaryRowSkeleton(
                color = color,
                position = when (it) {
                    0 -> DaySummaryPosition.First
                    rows - 1 -> DaySummaryPosition.Last
                    else -> DaySummaryPosition.Middle
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}