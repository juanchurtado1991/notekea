package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.withFrameNanos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** How long a note shown again may take to be laid out before its place is given up: Preview draws a long note late. */
internal val SCROLL_RESTORE_LIMIT: Duration = 3.seconds

// How long the page must keep its height, at its place, to count as laid out.
private val SETTLED_TIME: Duration = 500.milliseconds

/**
 * Scrolls to [target] once the page is laid out far enough to get there (`ROADMAP.md` 1.11), waiting frame by frame for
 * up to [limit] of real time, however many frames that is, and keeps it there while the page settles: Preview can lay
 * a long note out again, shorter for a moment, which pulls the view up to its end, so the view goes back to [target]
 * once the page is long enough again. It stops as soon as the person scrolls. A note now shorter than [target] scrolls
 * to its end once the time is up. Before, the note stayed at the top on a slow computer (1.11-09 on CI).
 */
internal suspend fun ScrollState.scrollWhenLaidOut(
    target: Int,
    limit: Duration = SCROLL_RESTORE_LIMIT
) {
    val start = TimeSource.Monotonic.markNow()
    while (!hasRoom(target = target) && start.elapsedNow() < limit) withFrameNanos { }
    scrollTo(value = target)
    keepWhileSettling(target = target, isTimeLeft = { start.elapsedNow() < limit })
}

private suspend fun ScrollState.keepWhileSettling(
    target: Int,
    isTimeLeft: () -> Boolean
) {
    var steadySince = TimeSource.Monotonic.markNow()
    var height = maxValue
    var isPulledUp = false
    while (steadySince.elapsedNow() < SETTLED_TIME && isTimeLeft() && !isScrollInProgress) {
        withFrameNanos { }
        // Only a view the page pulled up to its end goes back: one the person moved stays where they put it.
        if (value < target && value == maxValue) isPulledUp = true
        if (isPulledUp && hasRoom(target = target)) {
            scrollTo(value = target)
            isPulledUp = false
        }
        if (maxValue != height || value != target) steadySince = TimeSource.Monotonic.markNow()
        height = maxValue
    }
}

// Until the page is first laid out its height is unknown and maxValue is Int.MAX_VALUE: room for anything.
private fun ScrollState.hasRoom(target: Int): Boolean = maxValue != Int.MAX_VALUE && maxValue >= target
