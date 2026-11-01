package com.agentlearning.domain.model

import java.time.LocalDate

object ProgressCalculator {

    /** Correct answers / total answers, or null when nothing has been answered. */
    fun accuracy(correct: Int, total: Int): Float? =
        if (total <= 0) null else correct.toFloat() / total

    fun status(total: Int, answered: Int): LearningStatus = when {
        total <= 0 || answered <= 0 -> LearningStatus.NOT_STARTED
        answered >= total -> LearningStatus.COMPLETED
        else -> LearningStatus.IN_PROGRESS
    }

    /** Weighted mastery in 0..1; unanswered questions count as 0. */
    fun masteryScore(total: Int, mastered: Int, reviewing: Int, learning: Int): Float {
        if (total <= 0) return 0f
        val score = mastered * 1.0f + reviewing * 0.5f + learning * 0.2f
        return (score / total).coerceIn(0f, 1f)
    }

    /** Consecutive study days ending today; a streak that ended yesterday still counts. */
    fun streakDays(studyDays: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in studyDays) today else today.minusDays(1)
        var streak = 0
        while (cursor in studyDays) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
