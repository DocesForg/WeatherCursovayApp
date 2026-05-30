package com.docesforg.bura.graphs.temperature

import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.temperature.Temperature
import com.docesforg.bura.temperature.TemperatureMoment
import com.docesforg.bura.temperature.TemperaturePeriod
import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.graphs.common.GraphTime
import java.time.LocalDate
import java.time.LocalDateTime

fun getTemperatureGraphs(
    now: LocalDateTime,
    tempPeriod: TemperaturePeriod,
    condPeriod: ConditionPeriod
): ForecastResult<TemperatureGraphs> {
    val tempDays = tempPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    val conditionDays = condPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    return ForecastResult.Success(
        data = TemperatureGraphs(
            minTemp = tempPeriod.minimum,
            maxTemp = tempPeriod.maximum,
            graphs = getGraphs(
                now = now,
                tempDays = tempDays,
                conditionDays = conditionDays
            )
        )
    )
}

private fun getGraphs(
    now: LocalDateTime,
    tempDays: List<TemperaturePeriod>,
    conditionDays: List<ConditionPeriod>
): List<TemperatureGraph> = buildList {
    for (i in tempDays.indices) {
        add(
            getGraph(
                now = now,
                tempDay = tempDays[i],
                conditionDay = conditionDays[i],
                nextTempDay = tempDays.getOrNull(i + 1),
                nextConditionDay = conditionDays.getOrNull(i + 1)
            )
        )
    }
}

private fun getGraph(
    now: LocalDateTime,
    tempDay: TemperaturePeriod,
    conditionDay: ConditionPeriod,
    nextTempDay: TemperaturePeriod?,
    nextConditionDay: ConditionPeriod?
): TemperatureGraph {
    val minTempMoment = tempDay.reversed().minBy { it.temperature }
    val maxTempMoment = tempDay.reversed().maxBy { it.temperature }
    return TemperatureGraph(
        day = tempDay.first().hour.toLocalDate(),
        points = buildList {
            for (i in tempDay.indices) {
                add(
                    getPoint(
                        now = now,
                        tempMoment = tempDay[i],
                        minTempMoment = minTempMoment,
                        maxTempMoment = maxTempMoment,
                        conditionMoment = conditionDay[i]
                    )
                )
            }
            val firstTempTomorrow = nextTempDay?.firstOrNull()
            if (firstTempTomorrow != null) {
                // The periods must match, so if there is a first temp tomorrow, there
                // must be a matching condition tomorrow too
                val firstConditionTomorrow = nextConditionDay!!.first()
                add(
                    getPoint(
                        now = now,
                        tempMoment = firstTempTomorrow,
                        minTempMoment = minTempMoment,
                        maxTempMoment = maxTempMoment,
                        conditionMoment = firstConditionTomorrow
                    )
                )
            }
        }
    )
}

private fun getPoint(
    now: LocalDateTime,
    tempMoment: TemperatureMoment,
    minTempMoment: TemperatureMoment,
    maxTempMoment: TemperatureMoment,
    conditionMoment: ConditionMoment
): TemperatureGraphPoint = TemperatureGraphPoint(
    time = GraphTime(tempMoment.hour, now),
    temperature = GraphTemperature(
        value = tempMoment.temperature,
        meta = getTempMeta(minTempMoment, maxTempMoment, tempMoment)
    ),
    condition = conditionMoment.condition,
)

private fun getTempMeta(
    minTempMoment: TemperatureMoment,
    maxTempMoment: TemperatureMoment,
    tempMoment: TemperatureMoment
): GraphTemperature.Meta =
    when {
        minTempMoment == maxTempMoment -> GraphTemperature.Meta.Regular
        tempMoment == minTempMoment -> GraphTemperature.Meta.Minimum
        tempMoment == maxTempMoment -> GraphTemperature.Meta.Maximum
        else -> GraphTemperature.Meta.Regular
    }

data class TemperatureGraphs(
    val minTemp: Temperature,
    val maxTemp: Temperature,
    val graphs: List<TemperatureGraph>
)

data class TemperatureGraph(
    val day: LocalDate,
    val points: List<TemperatureGraphPoint>
)

data class TemperatureGraphPoint(
    val time: GraphTime,
    val temperature: GraphTemperature,
    val condition: Condition
)

data class GraphTemperature(
    val value: Temperature,
    val meta: Meta
) {
    enum class Meta {
        Minimum, Maximum, Regular
    }
}