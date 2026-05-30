package com.docesforg.bura.temperature

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.docesforg.bura.R
import com.docesforg.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

fun Temperature.string(context: Context, numberFormat: NumberFormat): String = context.getString(
    R.string.temp_value_degree,
    numberFormat.format(BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP))
)

@Composable
fun Temperature.string(): String = string(LocalContext.current, rememberNumberFormat())