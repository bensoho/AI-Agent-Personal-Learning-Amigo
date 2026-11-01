package com.agentlearning.domain

import com.agentlearning.domain.model.LearningStatus
import com.agentlearning.domain.model.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ProgressCalculatorTest {

    @Test
    fun accuracy_isNullWithoutAnswers() {
        assertNull(ProgressCalculator.accuracy(0, 0))
        assertEquals(0.75f, ProgressCalculator.accuracy(3, 4)!!, 0.0001f)
    }

    @Test
    fun status_followsAnsweredCount() {
        assertEquals(LearningStatus.NOT_STARTED, ProgressCalculator.status(0, 0))
        assertEquals(LearningStatus.NOT_STARTED, ProgressCalculator.status(5, 0))
        assertEquals(LearningStatus.IN_PROGRESS, ProgressCalculator.status(5, 2))
        assertEquals(LearningStatus.COMPLETED, ProgressCalculator.status(5, 5))
    }

    @Test
    fun masteryScore_weightsStatuses() {
        assertEquals(0f, ProgressCalculator.masteryScore(0, 0, 0, 0), 0f)
        assertEquals(0.5f, ProgressCalculator.masteryScore(4, 1, 2, 0), 0.0001f)
    }

    @Test
    fun streak_countsConsecutiveDays() {
        val today = LocalDate.parse("2026-01-10")
        val days = setOf("2026-01-10", "2026-01-09", "2026-01-08", "2026-01-05").map { LocalDate.parse(it) }.toSet()
        assertEquals(3, ProgressCalculator.streakDays(days, today))
    }

    @Test
    fun streak_survivesUntilEndOfNextDay() {
        val today = LocalDate.parse("2026-01-10")
        val days = setOf(LocalDate.parse("2026-01-09"), LocalDate.parse("2026-01-08"))
        assertEquals(2, ProgressCalculator.streakDays(days, today))
        assertEquals(0, ProgressCalculator.streakDays(days, today.plusDays(1)))
    }
}
