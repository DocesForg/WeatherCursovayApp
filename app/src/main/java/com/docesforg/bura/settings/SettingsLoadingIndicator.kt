package com.docesforg.bura.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.docesforg.bura.common.TextSkeleton
import com.docesforg.bura.common.animateShimmerColorAsState

@Composable
fun SettingsLoadingIndicator(modifier: Modifier = Modifier) {
    val shimmerColor = animateShimmerColorAsState()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 24.dp),
        userScrollEnabled = false
    ) {
        item {
            TextSkeleton(
                color = shimmerColor,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .width(100.dp)
                    .padding(horizontal = 16.dp)
            )
        }
        items(7) {
            PreferenceButtonSkeleton(color = shimmerColor)
        }
    }
}