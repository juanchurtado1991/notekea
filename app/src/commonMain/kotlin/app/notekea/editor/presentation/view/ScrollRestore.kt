package app.notekea.editor.presentation.view

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
 * Scrolls to [target] once the page is laid out far enough, waiting frame by frame for up to [limit] of real time,
 * and keeps it there while the page settles (Preview can lay a long note out again, shorter for a moment). It stops
 * as soon as the person scrolls; a note shorter than [target] scrolls to its end once the time is up.
 */
internal suspend fun ScrollState.scrollWhenLaidOut(
    target: Int,
    limit: Duration = SCROLL_RESTORE_LIMIT
) {
    val start = TimeSource.Monotonic.markNow()

    fun isWaitingForRoom(): Boolean = !hasRoom(target = target) && start.elapsedNow() < limit

    while (isWaitingForRoom()) withFrameNanos { }
    scrollTo(value = target)

    keepWhileSettling(
        target = target,
        isTimeLeft = {
            start.elapsedNow() < limit
        }
    )
}

private suspend fun ScrollState.keepWhileSettling(
    target: Int,
    isTimeLeft: () -> Boolean
) {
    var steadySince = TimeSource.Monotonic.markNow()
    var height = maxValue
    var isPulledUp = false

    fun isSettling(): Boolean =
        steadySince.elapsedNow() < SETTLED_TIME &&
            isTimeLeft() &&
            !isScrollInProgress

    while (isSettling()) {
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
private fun ScrollState.hasRoom(target: Int): Boolean =
    maxValue != Int.MAX_VALUE && maxValue >= target
