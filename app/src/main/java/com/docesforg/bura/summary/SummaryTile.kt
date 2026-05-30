package com.docesforg.bura.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SummaryTile(
    label: @Composable () -> Unit,
    value: @Composable () -> Unit,
    bottom: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    supportingValue: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(modifier) {
        val content = @Composable {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Column {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleSmall,
                        LocalContentColor provides MaterialTheme.colorScheme.secondary,
                        content = label
                    )
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.headlineMedium,
                        content = value
                    )
                    supportingValue?.let {
                        CompositionLocalProvider(
                            LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                            content = it
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    content = bottom
                )
            }
        }
        if (onClick != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minWidth),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                onClick = onClick,
                content = content
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minWidth),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                content = content
            )
        }
    }
}