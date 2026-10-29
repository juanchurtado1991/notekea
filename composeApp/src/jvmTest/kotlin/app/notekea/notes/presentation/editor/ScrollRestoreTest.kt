package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * A note's tab shown again goes back to where it was (`ROADMAP.md` 1.11) even when its page is laid out late, as
 * Preview lays out a long note on a slow computer: 1.11-09 of the Self-Test failed on CI, the scroll coming before the
 * page's first layout (when `maxValue` is still `Int.MAX_VALUE`) or after 30 frames of waiting.
 */
@OptIn(ExperimentalTestApi::class)
class ScrollRestoreTest {
    @Test
    fun `a page laid out late still scrolls back to its place`() = runComposeUiTest {
        val scrollState = ScrollState(initial = 0)
        var isLaidOut by mutableStateOf(value = false)
        setContent {
            Column(modifier = Modifier.height(height = VIEWPORT.dp).verticalScroll(state = scrollState)) {
                Spacer(modifier = Modifier.height(height = if (isLaidOut) PAGE.dp else VIEWPORT.dp))
            }
            LaunchedEffect(Unit) { scrollState.scrollWhenLaidOut(target = TARGET) }
            // The page gets its full height only after a while of real time, many frames of the test's clock.
            LaunchedEffect(Unit) {
                withContext(context = Dispatchers.IO) { Thread.sleep(LATE_MILLIS) }
                isLaidOut = true
            }
        }

        pumpFrames(until = { scrollState.value == TARGET }, millis = WAIT_MILLIS)

        assertEquals(TARGET, scrollState.value)
    }

    @Test
    fun `a page laid out again shorter for a moment, as Preview does, goes back to its place`() = runComposeUiTest {
        val scrollState = ScrollState(initial = 0)
        var height by mutableStateOf(value = PAGE)
        setContent {
            Column(modifier = Modifier.height(height = VIEWPORT.dp).verticalScroll(state = scrollState)) {
                Spacer(modifier = Modifier.height(height = height.dp))
            }
            LaunchedEffect(Unit) { scrollState.scrollWhenLaidOut(target = TARGET) }
            // What 1.11-09 recorded on CI: the page at full height, then shorter than the place, then full again.
            LaunchedEffect(Unit) {
                withContext(context = Dispatchers.IO) { Thread.sleep(SHRINK_MILLIS) }
                height = SHORT_PAGE
                withContext(context = Dispatchers.IO) { Thread.sleep(SHRINK_MILLIS) }
                height = PAGE
            }
        }

        pumpFrames(until = { false }, millis = SHRINK_MILLIS * 3)

        assertEquals(TARGET, scrollState.value)
    }

    @Test
    fun `a page shorter than the place scrolls to its end once the time is up`() = runComposeUiTest {
        val scrollState = ScrollState(initial = 0)
        setContent {
            Column(modifier = Modifier.height(height = VIEWPORT.dp).verticalScroll(state = scrollState)) {
                Spacer(modifier = Modifier.height(height = SHORT_PAGE.dp))
            }
            LaunchedEffect(Unit) { scrollState.scrollWhenLaidOut(target = TARGET, limit = 200.milliseconds) }
        }

        waitUntil(timeoutMillis = WAIT_MILLIS) { scrollState.value == scrollState.maxValue && scrollState.value > 0 }

        assertEquals(scrollState.maxValue, scrollState.value)
    }

    /** Frames keep coming while real time passes, as on screen, for [millis] or [until] it holds. */
    private fun ComposeUiTest.pumpFrames(
        until: () -> Boolean,
        millis: Long
    ) {
        val start = System.nanoTime()
        while (!until() && System.nanoTime() - start < millis * NANOS_PER_MILLI) {
            mainClock.advanceTimeByFrame()
            Thread.sleep(FRAME_MILLIS)
        }
    }

    private companion object {
        const val VIEWPORT = 100
        const val PAGE = 3_000
        const val SHORT_PAGE = 300
        const val TARGET = 1_000
        const val LATE_MILLIS = 800L
        const val SHRINK_MILLIS = 300L
        const val WAIT_MILLIS = 5_000L
        const val FRAME_MILLIS = 16L
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
