package com.agentlearning.domain.model

import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class ReviewScheduler @Inject constructor(private val zone: ZoneId) {

    /**
     * Returns the next review state after an answer.
     * A correct answer given before the question is due does not advance the schedule,
     * so repeating a question on the same day cannot inflate mastery.
     */
    fun schedule(current: ReviewState?, isCorrect: Boolean, nowMillis: Long): ReviewState {
        val ease = current?.easeFactor ?: DEFAULT_EASE

        if (!isCorrect) {
            return ReviewState(
                repetitionCount = 0,
                intervalDays = nextIntervalDays(false, 0),
                easeFactor = (ease - 0.2f).coerceAtLeast(MIN_EASE),
                nextReviewAt = startOfDayAfter(nowMillis, nextIntervalDays(false, 0)),
                lastReviewedAt = nowMillis,
                status = ReviewStatus.LEARNING,
            )
        }

        if (current != null && nowMillis < current.nextReviewAt) {
            return current.copy(lastReviewedAt = nowMillis)
        }

        val repetitions = (current?.repetitionCount ?: 0) + 1
        val interval = nextIntervalDays(true, repetitions)
        return ReviewState(
            repetitionCount = repetitions,
            intervalDays = interval,
            easeFactor = (ease + 0.05f).coerceAtMost(MAX_EASE),
            nextReviewAt = startOfDayAfter(nowMillis, interval),
            lastReviewedAt = nowMillis,
            status = if (repetitions >= MASTERED_THRESHOLD) ReviewStatus.MASTERED else ReviewStatus.REVIEWING,
        )
    }

    /** [consecutiveCorrect] counts the answer being scored (1 for the first correct answer). */
    fun nextIntervalDays(isCorrect: Boolean, consecutiveCorrect: Int): Int {
        if (!isCorrect) return 1
        return when (consecutiveCorrect) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 14
            else -> 30
        }
    }

    private fun startOfDayAfter(nowMillis: Long, days: Int): Long =
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
            .plusDays(days.toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

    companion object {
        const val DEFAULT_EASE = 2.5f
        const val MIN_EASE = 1.3f
        const val MAX_EASE = 3.0f
        const val MASTERED_THRESHOLD = 4
    }
}
