package com.docesforg.bura.summary

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.docesforg.bura.R
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.string
import com.docesforg.bura.common.AppTheme

@Composable
fun PopAndDrop(pop: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodySmall
    val color = MaterialTheme.colorScheme.primary
    val inlineContentMap = mapOf(
        "drop" to InlineTextContent(
            placeholder = Placeholder(
                width = style.fontSize,
                height = style.fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.water_drop),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = color
            )
        }
    )
    val annotatedString = buildAnnotatedString {
        withStyle(style.toSpanStyle()) {
            appendInlineContent(id = "drop")
            append(pop)
        }
    }
    Text(
        text = annotatedString,
        inlineContent = inlineContentMap,
        color = color,
        modifier = modifier
    )
}

@Preview
@Composable
private fun PopPreview() {
    AppTheme {
        PopAndDrop(pop = Pop(15.0).string())
    }
}