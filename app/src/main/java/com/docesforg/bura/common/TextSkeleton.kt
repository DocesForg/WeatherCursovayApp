package com.docesforg.bura.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle

@Composable
fun TextSkeleton(
    color: State<Color>,
    shape: Shape,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    contentPadding: PaddingValues = PaddingValues()
) {
    Box(modifier = modifier.height(IntrinsicSize.Min)) {
        Text(text = "", style = style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(color = color.value, shape = shape)
        )
    }
}