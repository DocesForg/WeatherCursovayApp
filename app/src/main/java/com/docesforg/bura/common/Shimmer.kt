package com.docesforg.bura.common

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun animateShimmerColorAsState(): State<Color> {
    val transition = rememberInfiniteTransition(label = "Shimmer loop")
    return transition.animateColor(
        initialValue = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        targetValue = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1000),
            RepeatMode.Reverse
        ),
        label = "Shimmer color"
    )
}