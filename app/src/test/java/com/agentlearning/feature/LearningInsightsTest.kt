package com.agentlearning.feature

import com.agentlearning.domain.model.Course
import com.agentlearning.domain.model.CourseOutline
import com.agentlearning.domain.model.DailyStat
import com.agentlearning.domain.model.Topic
import com.agentlearning.domain.model.TopicProgress
import com.agentlearning.domain.model.WeekOutline
import com.agentlearning.feature.statistics.consolidationTopics
import com.agentlearning.feature.statistics.weekActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class LearningInsightsTest {
    @Test
    fun weekActivity_usesMondayBoundaryNotRollingSevenDays() {
        val monday = LocalDate.of(2026, 11, 2)
        val result = weekActivity(
            listOf(
                DailyStat(monday.minusDays(1), 20, 10),
                DailyStat(monday, 4, 3),
                DailyStat(monday.plusDays(1), 99, 99),
            ),
            monday,
        )
        assertEquals(4, result.answers)
        assertEquals(3, result.correct)
        assertEquals(1, result.activeDays)
        assertEquals(0.75f, result.accuracy!!, 0f)
    }

    @Test
    fun weekActivity_countsOnlyActiveDaysAndHandlesYearBoundary() {
        val today = LocalDate.of(2027, 1, 1)
        val result = weekActivity(
            listOf(
                DailyStat(LocalDate.of(2026, 12, 27), 10, 10),
                DailyStat(LocalDate.of(2026, 12, 28), 5, 3),
                DailyStat(LocalDate.of(2026, 12, 29), 0, 0),
                DailyStat(today, 3, 1),
            ),
            today,
        )
        assertEquals(8, result.answers)
        assertEquals(4, result.correct)
        assertEquals(2, result.activeDays)
        assertEquals(0.5f, result.accuracy!!, 0f)
        assertNull(weekActivity(emptyList(), today).accuracy)
    }

    @Test
    fun weekActivity_sundayIncludesAllSevenDaysAndWeightsAnswers() {
        val sunday = LocalDate.of(2026, 11, 1)
        val result = weekActivity(
            listOf(
                DailyStat(sunday.minusDays(6), 9, 9),
                DailyStat(sunday.minusDays(1), 0, 0),
                DailyStat(sunday, 1, 0),
            ),
            sunday,
        )
        assertEquals(10, result.answers)
        assertEquals(2, result.activeDays)
        assertEquals(0.9f, result.accuracy!!, 0f)
    }

    @Test
    fun consolidation_excludesUnansweredAndRanksActualLearningQuestions() {
        fun topic(id: String, week: Int, answered: Int, learning: Int) = TopicProgress(
            Topic(id, "c", week, "Week $week", id, null),
            total = 10, answered = answered, mastered = 0, reviewing = 0, learning = learning,
        )
        val topics = listOf(
            topic("unanswered", 1, 0, 0),
            topic("mastered", 1, 5, 0),
            topic("small", 1, 1, 1),
            topic("later", 3, 4, 4),
            topic("earlier", 2, 4, 4),
            topic("medium", 4, 2, 2),
        )
        val course = CourseOutline(Course("c", "Course", 1), listOf(WeekOutline(1, "Week", topics)))
        assertEquals(listOf("earlier", "later", "medium"), consolidationTopics(listOf(course)).map { it.topic.id })
        assertEquals(emptyList<TopicProgress>(), consolidationTopics(emptyList()))
    }
}
