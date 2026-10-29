package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.withFrameNanos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** How long a note shown again may take to be laid out before its place is given up: Preview draws a long note late. */
internal val SCROLL_RESTORE_LIMIT: Duration = 3.seconds

/**
 * Scrolls to [target] once the page is laid out far enough to get there (`ROADMAP.md` 1.11), waiting frame by frame for
 * up to [limit] of real time, however many frames that is. The note stayed at the top when the scroll came before the
 * page's first layout, or when Preview took longer than a fixed count of frames to draw a long note, on a slow computer. A note now shorter than [target] scrolls to its
 * end once the time is up.
 */
internal suspend fun ScrollState.scrollWhenLaidOut(
    target: Int,
    limit: Duration = SCROLL_RESTORE_LIMIT
) {
    val start = TimeSource.Monotonic.markNow()
    // Until the page is first laid out its height is unknown and maxValue is Int.MAX_VALUE: room for anything.
    while ((maxValue == Int.MAX_VALUE || maxValue < target) && start.elapsedNow() < limit) withFrameNanos { }
    scrollTo(value = target)
}
