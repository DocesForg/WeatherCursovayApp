package com.docesforg.bura.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.docesforg.bura.common.Theme

@Composable
fun SettingsDestination(theme: Theme, onThemeClick: (Theme) -> Unit, onBackClick: () -> Unit) {
    val unitsVM = viewModel<SelectedUnitsViewModel>(factory = SelectedUnitsViewModel.Factory)
    LaunchedEffect(Unit) { unitsVM.getSettings() }
    SettingsScreen(
        units = unitsVM.state.collectAsState().value,
        theme = theme,
        onTemperatureUnitClick = unitsVM::selectTemperatureUnit,
        onWindUnitClick = unitsVM::selectWindUnit,
        onPrecipitationUnitClick = unitsVM::selectPrecipitationUnit,
        onRainUnitClick = unitsVM::selectRainUnit,
        onShowersUnitClick = unitsVM::selectShowersUnit,
        onSnowUnitClick = unitsVM::selectSnowUnit,
        onPressureUnitClick = unitsVM::selectPressureUnit,
        onVisibilityUnitClick = unitsVM::selectVisibilityUnit,
        onThemeClick = onThemeClick,
        onBackClick = onBackClick
    )
}