package com.agentlearning.feature

import com.agentlearning.feature.knowledge.READ_THRESHOLD_MS
import com.agentlearning.feature.knowledge.ReadTimer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadTimerTest {
    private var now = 0L
    private val timer = ReadTimer { now }

    @Test
    fun `visit shorter than threshold is not a read`() {
        timer.resume()
        now += READ_THRESHOLD_MS - 1
        assertFalse(timer.finish())
    }

    @Test
    fun `visit reaching threshold counts once finished`() {
        timer.resume()
        now += READ_THRESHOLD_MS
        assertTrue(timer.finish())
    }

    @Test
    fun `time while paused is excluded and foreground time accumulates`() {
        timer.resume()
        now += 20_000
        timer.pause()
        now += 60_000
        assertEquals(20_000, timer.elapsedMs)
        timer.resume()
        now += 10_000
        assertTrue(timer.finish())
    }

    @Test
    fun `repeated resume and pause are idempotent`() {
        timer.resume()
        now += 5_000
        timer.resume()
        now += 5_000
        timer.pause()
        timer.pause()
        assertEquals(10_000, timer.elapsedMs)
    }
}
