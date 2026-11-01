package com.agentlearning.feature.statistics

import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.domain.model.ProgressCalculator
import com.agentlearning.domain.model.TopicProgress
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class WeekActivity(val answers: Int, val correct: Int, val activeDays: Int) {
    val accuracy: Float? get() = ProgressCalculator.accuracy(correct, answers)
}

fun weekActivity(daily: List<DailyStat>, today: LocalDate): WeekActivity {
    val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = daily.filter { !it.date.isBefore(start) && !it.date.isAfter(today) }
    return WeekActivity(days.sumOf { it.total }, days.sumOf { it.correct }, days.count { it.total > 0 })
}

fun consolidationTopics(courses: List<CourseOutline>): List<TopicProgress> =
    courses.flatMap { course -> course.weeks.flatMap { it.topics } }
        .filter { it.answered > 0 && it.learning > 0 }
        .sortedWith(compareByDescending<TopicProgress> { it.learning }.thenBy { it.topic.weekNumber }.thenBy { it.topic.id })
        .take(3)
