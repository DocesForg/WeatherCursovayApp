package com.docesforg.bura.graphs.pop

import com.docesforg.bura.condition.Condition
import com.docesforg.bura.condition.ConditionMoment
import com.docesforg.bura.condition.ConditionPeriod
import com.docesforg.bura.forecast.ForecastResult
import com.docesforg.bura.graphs.common.GraphTime
import com.docesforg.bura.pop.Pop
import com.docesforg.bura.pop.PopMoment
import com.docesforg.bura.pop.PopPeriod
import java.time.LocalDate
import java.time.LocalDateTime

fun getPopGraphs(
    now: LocalDateTime,
    popPeriod: PopPeriod,
    conditionPeriod: ConditionPeriod,
): ForecastResult<List<PopGraph>> {
    val popDays = popPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    val conditionDays = conditionPeriod.daysFrom(now.toLocalDate()) ?: return ForecastResult.Outdated
    return ForecastResult.Success(
        data = popDays.mapIndexed { idx, popDay ->
            val conditionDay = conditionDays[idx]
            val popTomorrow = popDays.getOrNull(idx + 1)
            val conditionTomorrow = conditionDays.getOrNull(idx + 1)
            getPopGraph(now, popDay, conditionDay, popTomorrow, conditionTomorrow)
        }
    )
}

private fun getPopGraph(
    now: LocalDateTime,
    popDay: PopPeriod,
    conditionDay: ConditionPeriod,
    popTomorrow: PopPeriod?,
    conditionTomorrow: ConditionPeriod?
): PopGraph {
    return PopGraph(
        day = popDay.first().hour.toLocalDate(),
        points = buildList {
            val firstPopTomorrow = popTomorrow?.first()
            val popDayAdjusted: PopPeriod
            val conditionDayAdjusted: ConditionPeriod
            if (firstPopTomorrow != null) {
                // To avoid an empty space at the end of every day, we add the first pop
                // of tomorrow for completeness
                val firstConditionTomorrow = conditionTomorrow!!.first()
                popDayAdjusted = PopPeriod(popDay + firstPopTomorrow)
                conditionDayAdjusted = ConditionPeriod(conditionDay + firstConditionTomorrow)
            } else {
                popDayAdjusted = popDay
                conditionDayAdjusted = conditionDay
            }
            val maxPop = popDayAdjusted.maxBy { it.pop }
            for (i in popDayAdjusted.indices) {
                val popMoment = popDayAdjusted[i]
                val conditionMoment = conditionDayAdjusted[i]
                add(getPoint(now, popMoment, maxPop, conditionMoment))
            }
        }
    )
}

private fun getPoint(
    now: LocalDateTime,
    moment: PopMoment,
    maxPopMoment: PopMoment,
    conditionMoment: ConditionMoment,
): PopGraphPoint = PopGraphPoint(
    time = GraphTime(moment.hour, now),
    pop = GraphPop(
        value = moment.pop,
        meta = if (moment == maxPopMoment) GraphPop.Meta.Maximum else GraphPop.Meta.Regular
    ),
    condition = conditionMoment.condition
)

data class PopGraph(
    val day: LocalDate,
    val points: List<PopGraphPoint>
)

data class PopGraphPoint(
    val time: GraphTime,
    val pop: GraphPop,
    val condition: Condition
)

data class GraphPop(
    val value: Pop,
    val meta: Meta
) {
    enum class Meta {
        Regular, Maximum
    }
}