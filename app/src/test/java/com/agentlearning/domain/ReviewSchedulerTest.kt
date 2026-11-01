package com.agentlearning.domain

import com.agentlearning.domain.model.ReviewScheduler
import com.agentlearning.domain.model.ReviewState
import com.agentlearning.domain.model.ReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ReviewSchedulerTest {
    private val zone: ZoneId = ZoneOffset.UTC
    private val scheduler = ReviewScheduler(zone)

    private fun at(date: String, hour: Int = 10): Long =
        LocalDate.parse(date).atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()

    private fun startOf(date: String): Long = LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()

    @Test
    fun intervals_followTheDocumentedTable() {
        assertEquals(1, scheduler.nextIntervalDays(true, 1))
        assertEquals(3, scheduler.nextIntervalDays(true, 2))
        assertEquals(7, scheduler.nextIntervalDays(true, 3))
        assertEquals(14, scheduler.nextIntervalDays(true, 4))
        assertEquals(30, scheduler.nextIntervalDays(true, 5))
        assertEquals(1, scheduler.nextIntervalDays(false, 3))
    }

    @Test
    fun firstCorrectAnswer_isDueTomorrowAtMidnight() {
        val state = scheduler.schedule(null, true, at("2026-01-10"))
        assertEquals(1, state.repetitionCount)
        assertEquals(ReviewStatus.REVIEWING, state.status)
        assertEquals(startOf("2026-01-11"), state.nextReviewAt)
    }

    @Test
    fun firstWrongAnswer_resetsAndIsDueTomorrow() {
        val state = scheduler.schedule(null, false, at("2026-01-10"))
        assertEquals(0, state.repetitionCount)
        assertEquals(ReviewStatus.LEARNING, state.status)
        assertEquals(startOf("2026-01-11"), state.nextReviewAt)
    }

    @Test
    fun consecutiveCorrectAnswersOnDueDays_growTheInterval() {
        var state = scheduler.schedule(null, true, at("2026-01-10"))
        state = scheduler.schedule(state, true, at("2026-01-11"))
        assertEquals(2, state.repetitionCount)
        assertEquals(startOf("2026-01-14"), state.nextReviewAt)
        state = scheduler.schedule(state, true, at("2026-01-14"))
        assertEquals(startOf("2026-01-21"), state.nextReviewAt)
    }

    @Test
    fun masteryRequiresFourSpacedCorrectAnswers() {
        var state: ReviewState? = null
        var day = LocalDate.parse("2026-01-01")
        repeat(3) {
            state = scheduler.schedule(state, true, day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli())
            assertEquals(ReviewStatus.REVIEWING, state!!.status)
            day = day.plusDays(state!!.intervalDays.toLong())
        }
        state = scheduler.schedule(state, true, day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli())
        assertEquals(ReviewStatus.MASTERED, state!!.status)
    }

    @Test
    fun correctAnswerBeforeDue_doesNotAdvanceSchedule() {
        val first = scheduler.schedule(null, true, at("2026-01-10", 9))
        val again = scheduler.schedule(first, true, at("2026-01-10", 18))
        assertEquals(first.repetitionCount, again.repetitionCount)
        assertEquals(first.nextReviewAt, again.nextReviewAt)
        assertEquals(at("2026-01-10", 18), again.lastReviewedAt)
    }

    @Test
    fun wrongAnswerOnMasteredQuestion_resetsProgress() {
        var state: ReviewState? = null
        var day = LocalDate.parse("2026-01-01")
        repeat(4) {
            state = scheduler.schedule(state, true, day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli())
            day = day.plusDays(state!!.intervalDays.toLong())
        }
        val reset = scheduler.schedule(state, false, day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli())
        assertEquals(0, reset.repetitionCount)
        assertEquals(ReviewStatus.LEARNING, reset.status)
    }
}
