package com.docesforg.bura.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.docesforg.bura.R

@Composable
fun FailedToDownloadErrorScreen(onTryAgainClick: () -> Unit, modifier: Modifier = Modifier) {
    ErrorScreen(
        text = {
            Text(stringResource(id = R.string.general_error_failed_to_download))
        },
        solution = {
            Button(onClick = onTryAgainClick) {
                Text(stringResource(id = R.string.general_btn_try_again))
            }
        },
        modifier = modifier
    )
}

@Composable
fun OutdatedErrorScreen(onTryAgainClick: () -> Unit, modifier: Modifier = Modifier) {
    ErrorScreen(
        text = {
            Text(stringResource(id = R.string.general_error_outdated))
        },
        solution = {
            Button(onClick = onTryAgainClick) {
                Text(stringResource(id = R.string.general_btn_try_again))
            }
        },
        modifier = modifier
    )
}

@Composable
fun NoSelectedPlaceErrorScreen(
    onSelectPlaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        text = {
            Text(stringResource(id = R.string.general_error_no_selected_place))
        },
        solution = {
            Button(onClick = onSelectPlaceClick) {
                Text(text = stringResource(id = R.string.general_btn_select_place))
            }
        },
        modifier = modifier
    )
}

@Composable
fun ErrorScreen(
    text: @Composable () -> Unit,
    solution: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center)) {
            text()
        }
        Spacer(modifier = Modifier.height(8.dp))
        solution?.let { it() }
    }
}