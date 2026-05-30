package com.docesforg.bura.visibility

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.docesforg.bura.R
import com.docesforg.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

private fun Visibility.valueString(numberFormat: NumberFormat): String =
    numberFormat.format(
        BigDecimal.valueOf(value).setScale(
            when (unit) {
                Visibility.Unit.Meters, Visibility.Unit.Feet -> 0
                Visibility.Unit.Kilometers, Visibility.Unit.Miles -> 1
            },
            RoundingMode.HALF_UP
        )
    )

private fun Visibility.unitString(context: Context): String = context.getString(
    when (unit) {
        Visibility.Unit.Meters -> R.string.vis_unit_m
        Visibility.Unit.Feet -> R.string.vis_unit_ft
        Visibility.Unit.Kilometers -> R.string.vis_unit_km
        Visibility.Unit.Miles -> R.string.vis_unit_mi
    }
)

@Composable
fun Visibility.valueString() = valueString(rememberNumberFormat())

@Composable
fun Visibility.unitString() = unitString(LocalContext.current)