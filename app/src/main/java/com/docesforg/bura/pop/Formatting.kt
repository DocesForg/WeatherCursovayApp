package com.docesforg.bura.pop

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.docesforg.bura.R
import com.docesforg.bura.common.rememberNumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

fun Pop.string(context: Context, numberFormat: NumberFormat): String = context.getString(
    R.string.pop_value_percent,
    numberFormat.format(BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP))
)

@Composable
fun Pop.string(): String = string(LocalContext.current, rememberNumberFormat())