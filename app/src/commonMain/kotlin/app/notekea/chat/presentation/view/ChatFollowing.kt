package app.notekea.chat.presentation.view

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.presentation.model.ChatEntry
import kotlinx.coroutines.flow.drop

// Further than any chat is long: the list stops at its end.
private const val TO_THE_END = 1_000_000f

/**
 * Keeps the newest of [entries] in view in [list] while it is written (the developer's smoke of 2026-11-13: the chat
 * did not follow the text): at each change the list goes to its end, unless the person scrolled up to read, and
 * again once they are back at the end or ask something new.
 */
@Composable
internal fun FollowNewest(
    list: LazyListState,
    entries: List<ChatEntry>
) {
    val shown by rememberUpdatedState(newValue = entries)
    // Read by the effects alone, never by what is drawn.
    val follows = remember { mutableStateOf(value = true) }
    val isMoving = remember { mutableStateOf(value = false) }

    // Where the person's own scroll ends says whether they left the end; the text growing under them is no scroll.
    LaunchedEffect(list) {
        snapshotFlow { list.isScrollInProgress }.drop(count = 1).collect { isScrolling ->
            val isTheirs = !isScrolling && !isMoving.value
            if (isTheirs) follows.value = !list.canScrollForward
        }
    }

    // One change after another, each scroll ended before the next: none is cut halfway, short of the end.
    LaunchedEffect(list) {
        var size = 0

        // Also what the list measures: Markdown is laid out a moment after its text changes.
        snapshotFlow {
            val measured = list
                .layoutInfo
                .visibleItemsInfo
                .sumOf { it.size }

            Triple(shown.size, shown.lastOrNull(), measured)
        }.collect { (count, _, _) ->
            if (count != size) follows.value = true
            size = count
            val isFollowed = follows.value && count > 0
            if (!isFollowed) return@collect
            isMoving.value = true

            try {
                toTheEnd(
                    list = list,
                    last = count - 1
                )
            } finally {
                isMoving.value = false
            }
        }
    }
}

private suspend fun toTheEnd(
    list: LazyListState,
    last: Int
) {
    // The new text is measured by the next frame; then to the very end of it.
    withFrameNanos { }
    val info = list.layoutInfo

    val isInView = info.visibleItemsInfo.any {
        it.index == last
    }

    val isFar = !isInView && info.totalItemsCount > last
    if (isFar) list.scrollToItem(index = last)
    list.scrollBy(value = TO_THE_END)
}

// It draws nothing.
@Preview
@Composable
private fun FollowNewestPreview() {
    FollowNewest(
        list = rememberLazyListState(),
        entries = emptyList()
    )
}
