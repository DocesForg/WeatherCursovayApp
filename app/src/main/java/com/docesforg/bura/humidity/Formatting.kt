package com.docesforg.bura.humidity

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.docesforg.bura.R
import com.docesforg.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

private fun Humidity.string(context: Context, numberFormat: NumberFormat): String =
    context.getString(
        R.string.humidity_value_percent,
        numberFormat.format(BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP))
    )

@Composable
fun Humidity.string() = string(LocalContext.current, rememberNumberFormat())