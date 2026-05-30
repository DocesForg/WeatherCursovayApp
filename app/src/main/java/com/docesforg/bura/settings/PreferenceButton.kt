package com.docesforg.bura.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.docesforg.bura.common.TextSkeleton

@Composable
fun PreferenceButton(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    PreferenceButton(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        value = {
            Text(
                text = value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onClick = onClick
    )
}

@Composable
fun PreferenceButtonSkeleton(color: State<Color>) {
    PreferenceButton(
        title = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(140.dp)
            )
        },
        value = {
            TextSkeleton(
                color = color,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.width(160.dp)
            )
        },
        onClick = null
    )
}

@Composable
private fun PreferenceButton(
    title: @Composable () -> Unit,
    value: @Composable () -> Unit,
    onClick: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else Modifier
            )
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyLarge,
            content = title
        )
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            content = value
        )
    }
}