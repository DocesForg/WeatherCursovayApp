package com.docesforg.bura.summary

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun ValueAndUnit(
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    unitStyle: TextStyle = MaterialTheme.typography.headlineSmall
) {
    val annotatedString = buildAnnotatedString {
        withStyle(valueStyle.toSpanStyle()) { append(value) }
        withStyle(unitStyle.toSpanStyle()) { append(" $unit") }
    }
    Text(text = annotatedString, modifier)
}