package com.docesforg.bura.summary.visibility

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
import com.docesforg.bura.summary.ValueAndUnit
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.visibility.unitString
import com.docesforg.bura.visibility.valueString

@Composable
fun VisibilitySummary(state: VisibilitySummary, modifier: Modifier = Modifier) {
    SummaryTile(
        label = { Text(stringResource(R.string.vis)) },
        value = {
            ValueAndUnit(
                value = state.now.valueString(),
                unit = state.now.unitString()
            )
        },
        bottom = {
            Text(
                stringResource(
                    when (state.now.description) {
                        Visibility.Description.VeryLow -> R.string.vis_description_very_low
                        Visibility.Description.Low -> R.string.vis_description_low
                        Visibility.Description.Fair -> R.string.vis_description_fair
                        Visibility.Description.Clear -> R.string.vis_description_clear
                        Visibility.Description.Perfect -> R.string.vis_description_perfect
                    }
                )
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun VisibilitySummaryPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .size(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VisibilitySummary(
                state = VisibilitySummary(
                    Visibility(1020.0, Visibility.Unit.Meters).convertTo(Visibility.Unit.Kilometers)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            VisibilitySummary(
                state = VisibilitySummary(
                    Visibility(90.0, Visibility.Unit.Meters).convertTo(Visibility.Unit.Kilometers)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}