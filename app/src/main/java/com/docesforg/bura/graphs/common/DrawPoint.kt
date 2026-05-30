package com.docesforg.bura.graphs.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.LayoutDirection

fun DrawScope.drawPoint(
    center: Offset,
    args: GraphArgs
) {
    val outlineRadius = args.pointCenterRadius + (args.pointOutlineWidth / 2)
    drawCircle(
        color = args.pointOutlineColor,
        radius = outlineRadius,
        center = center,
        style = Stroke(width = args.pointOutlineWidth)
    )
    drawCircle(
        color = args.pointCenterColor,
        radius = args.pointCenterRadius,
        center = center,
        style = Fill
    )
}

fun DrawScope.drawLabeledPoint(
    label: String,
    center: Offset,
    args: GraphArgs,
    measurer: TextMeasurer
) {
    drawPointLabel(
        label, measurer,
        pointCenter = center,
        args = args
    )
    drawPoint(
        center,
        args = args
    )
}

private fun DrawScope.drawPointLabel(
    text: String,
    measurer: TextMeasurer,
    pointCenter: Offset,
    args: GraphArgs,
) {
    val labelMeasured = measurer.measure(text, args.axisTextStyle.copy(color = args.pointLabelColor))
    val textTopLeftX = pointCenter.x - labelMeasured.size.width / 2
    val textTopLeftXMin =
        if (layoutDirection == LayoutDirection.Ltr) args.startGutter + args.textPaddingMinHorizontal
        else args.endGutter + args.textPaddingMinHorizontal
    val textTopLeftXMax =
        if (layoutDirection == LayoutDirection.Ltr) size.width - labelMeasured.size.width - args.endGutter - args.textPaddingMinHorizontal
        else size.width - labelMeasured.size.width - args.startGutter - args.textPaddingMinHorizontal
    drawText(
        textLayoutResult = labelMeasured,
        topLeft = Offset(
            x = textTopLeftX.coerceIn(
                minimumValue = textTopLeftXMin,
                maximumValue = textTopLeftXMax
            ),
            y = pointCenter.y - (labelMeasured.size.height) - args.pointTextPaddingBottom
        )
    )
}