package com.docesforg.bura.graphs.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.LayoutDirection

fun DrawScope.drawPlotLinePath(lastX: Float, args: GraphArgs, block: DrawScope.() -> Unit) {
    clipPath(
        path = Path().apply {
            val rectStartX =
                if (layoutDirection == LayoutDirection.Ltr) args.startGutter
                else lastX
            val rectWidth =
                if (layoutDirection == LayoutDirection.Ltr) lastX - args.startGutter
                else size.width - args.startGutter
            addRect(
                Rect(
                    offset = Offset(x = rectStartX, y = args.topGutter),
                    size = Size(
                        width = rectWidth,
                        height = size.height - args.topGutter - args.bottomGutter
                    )
                )
            )
        },
        block = block
    )
}