package com.docesforg.bura.graphs.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection

fun DrawScope.drawPastOverlay(
    nowX: Float,
    args: GraphArgs
) {
    drawLine(
        color = args.axisColor,
        start = Offset(x = nowX, y = 0f),
        end = Offset(x = nowX, y = size.height),
        strokeWidth = 2f
    )
    val topLeftX = if (layoutDirection == LayoutDirection.Ltr) 0f else nowX
    val overlayWidth = if (layoutDirection == LayoutDirection.Ltr) nowX else size.width - nowX
    drawRect(
        color = args.pastOverlayColor,
        topLeft = Offset(topLeftX, 0f),
        size = Size(width = overlayWidth, height = size.height)
    )
}

fun DrawScope.drawPastOverlayWithPoint(
    nowCenter: Offset,
    args: GraphArgs
) {
    drawPastOverlay(nowCenter.x, args)
    drawPoint(nowCenter, args)
}