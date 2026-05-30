package com.docesforg.bura.graphs.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.LayoutDirection

fun <T> DrawScope.drawVerticalAxis(
    steps: List<T>,
    args: GraphArgs,
    measurer: TextMeasurer,
    stepFormatter: (step: T) -> String?,
) {
    val lineX =
        if (layoutDirection == LayoutDirection.Ltr) size.width - args.endGutter
        else args.endGutter
    for (i in 0..steps.lastIndex) {
        val stepFraction = i.toDouble() / steps.lastIndex
        val plotBottom = size.height - args.bottomGutter
        val plotHeight = size.height - args.topGutter - args.bottomGutter
        val stepY = (plotBottom - plotHeight * stepFraction).toFloat()

        val horizontalLineStartX =
            if (layoutDirection == LayoutDirection.Ltr) args.startGutter
            else size.width - args.startGutter
        drawLine(
            color = args.axisColor,
            start = Offset(horizontalLineStartX, stepY),
            end = Offset(lineX, stepY)
        )

        val measuredText = measurer.measure(
            text = stepFormatter(steps[i]) ?: continue,
            style = args.axisTextStyle
        )
        val textTopLeftX =
            if (layoutDirection == LayoutDirection.Ltr) lineX + args.endAxisTextPaddingStart
            else lineX - args.endAxisTextPaddingStart - measuredText.size.width
        drawText(
            textLayoutResult = measuredText,
            color = args.axisColor,
            topLeft = Offset(
                x = textTopLeftX,
                y = stepY - (measuredText.size.height / 2)
            )
        )
    }
}