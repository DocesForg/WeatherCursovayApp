package com.docesforg.bura.graphs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

@Composable
fun EssentialGraphsDestination(
    initialDay: LocalDate?,
    onSelectPlaceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel =
        viewModel<EssentialGraphsViewModel>(factory = EssentialGraphsViewModel.Factory)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.getGraphs()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    EssentialGraphsScreen(
        initialDay = initialDay,
        state = viewModel.state.collectAsState().value,
        onTryAgainClick = viewModel::getGraphs,
        onSelectPlaceClick = onSelectPlaceClick,
        onBackClick = onBackClick
    )
}