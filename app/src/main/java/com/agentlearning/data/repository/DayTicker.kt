package com.agentlearning.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.LocalDate

/**
 * Emits today's date immediately and again after each local midnight, so queries bound to
 * "today" or "now" can be re-subscribed instead of keeping the value captured at subscription.
 */
internal fun Clock.dayChanges(): Flow<LocalDate> {
    val clock = this
    return flow {
        while (true) {
            val today = LocalDate.now(clock)
            emit(today)
            val nextMidnight = today.plusDays(1).atStartOfDay(clock.zone).toInstant().toEpochMilli()
            delay((nextMidnight - clock.millis()).coerceAtLeast(0) + MIDNIGHT_MARGIN_MILLIS)
        }
    }.distinctUntilChanged()
}

private const val MIDNIGHT_MARGIN_MILLIS = 1_000L
