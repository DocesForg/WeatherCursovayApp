package com.docesforg.bura.graphs.precipitation

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.graphs.common.GraphTime
import com.docesforg.bura.precipitation.MixedPrecipitation
import com.docesforg.bura.precipitation.PrecipitationPeriod
import java.time.LocalDate
import java.time.LocalDateTime

fun getPrecipitationGraphs(
    now: LocalDateTime,
    precipPeriod: PrecipitationPeriod,
    condPeriod: ConditionPeriod
): ForecastResult<PrecipitationGraphs> {
    val precipDays = precipPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    val condDays = condPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    return ForecastResult.Success(
        data = PrecipitationGraphs(
            max = precipDays.maxOf { it.max },
            graphs = precipDays.mapIndexed { dayIdx, day ->
                PrecipitationGraph(
                    day = day.first().hour.toLocalDate(),
                    points = buildList {
                        addAll(
                            day.mapIndexed { momentIdx, moment ->
                                PrecipitationGraphPoint(
                                    time = GraphTime(
                                        hour = moment.hour,
                                        now = now
                                    ),
                                    precip = moment.precipitation,
                                    cond = condDays[dayIdx][momentIdx].condition
                                )
                            }
                        )
                    }
                )
            }
        )
    )
}

data class PrecipitationGraphs(
    val max: MixedPrecipitation,
    val graphs: List<PrecipitationGraph>
)

data class PrecipitationGraph(
    val day: LocalDate,
    val points: List<PrecipitationGraphPoint>
)

data class PrecipitationGraphPoint(
    val time: GraphTime,
    val precip: MixedPrecipitation,
    val cond: Condition
)