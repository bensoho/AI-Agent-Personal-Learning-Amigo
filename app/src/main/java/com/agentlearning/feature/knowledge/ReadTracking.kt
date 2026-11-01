package com.agentlearning.feature.knowledge

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.agentlearning.ui.components.Pill

/** A detail page visible for at least this long counts as one read once the user leaves it. */
const val READ_THRESHOLD_MS = 30_000L

/** Accumulates the time a page is in the foreground; pausing and resuming are idempotent. */
class ReadTimer(private val now: () -> Long) {
    private var resumedAt: Long? = null
    private var accumulatedMs = 0L

    val elapsedMs: Long
        get() = accumulatedMs + (resumedAt?.let { now() - it } ?: 0L)

    fun resume() {
        if (resumedAt == null) resumedAt = now()
    }

    fun pause() {
        resumedAt?.let { accumulatedMs += now() - it }
        resumedAt = null
    }

    /** Stops timing and reports whether the visit was long enough to count as a read. */
    fun finish(thresholdMs: Long = READ_THRESHOLD_MS): Boolean {
        pause()
        return accumulatedMs >= thresholdMs
    }
}

/** Times the hosting screen only while it is resumed (not backgrounded or covered by another page). */
@Composable
fun TrackReadingTime(onResume: () -> Unit, onPause: () -> Unit) {
    LifecycleResumeEffect(Unit) {
        onResume()
        onPauseOrDispose { onPause() }
    }
}

@Composable
fun ReadCountPill(count: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    if (count > 0) {
        Pill("已读 $count 次", modifier, container = colors.tertiaryContainer, content = colors.onTertiaryContainer)
    } else {
        Pill("未读", modifier, container = colors.surfaceVariant, content = colors.onSurfaceVariant)
    }
}
