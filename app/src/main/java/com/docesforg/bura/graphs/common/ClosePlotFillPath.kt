package com.docesforg.bura.graphs.common

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection

fun DrawScope.closePlotFillPath(
    path: Path,
    lastX: Float,
    args: GraphArgs
) {
    val plotBottom = size.height - args.bottomGutter
    with(path) {
        lineTo(x = lastX, y = plotBottom)
        lineTo(x = if (layoutDirection == LayoutDirection.Ltr) args.startGutter else size.width - args.startGutter, y = plotBottom)
        close()
    }
}