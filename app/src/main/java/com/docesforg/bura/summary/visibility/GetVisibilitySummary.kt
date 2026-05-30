package com.docesforg.bura.summary.visibility

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.visibility.Visibility
import com.docesforg.bura.visibility.VisibilityPeriod
import java.time.LocalDateTime

fun getVisibilitySummary(
    now: LocalDateTime,
    visPeriod: VisibilityPeriod
): ForecastResult<VisibilitySummary> {
    return ForecastResult.Success(
        VisibilitySummary(
            now = visPeriod[now]?.visibility ?: return ForecastResult.Outdated
        )
    )
}

data class VisibilitySummary(val now: Visibility)