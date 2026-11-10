package app.notekea.editor.presentation.view

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.presentation.designsystem.theme.LocalReduceMotion
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.sidepanel.OutlineTracker
import app.notekea.editor.presentation.view.EditorPageState.Companion.LEADING_ITEMS
import app.notekea.editor.presentation.viewmodel.ScrollMemory
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val CURSOR_MARGIN_DP = 48
private const val HEADING_MARGIN_DP = 8
private const val MATCH_SCROLL_MARGIN_DP = 96

/**
 * Where the list is scrolled to. A note shown again goes back to where it was ([memory]), unless a place of it was
 * just selected, which is shown instead. The text field is laid out once even when it is out of view, so it can take
 * the keyboard. And when the cursor goes to a block out of view (a heading chosen, a mention opened), the list follows.
 */
@Composable
internal fun FollowScroll(
    state: EditorState,
    page: EditorPageState,
    memory: ScrollMemory?
) {
    val path = state.notePath
    val margin = with(LocalDensity.current) { CURSOR_MARGIN_DP.dp.toPx() }

    LaunchedEffect(page, path, state.isLoading) {
        if (path == null || state.isLoading) return@LaunchedEffect
        val list = page.listState

        snapshotFlow { list.layoutInfo.totalItemsCount }.first {
            it >= LEADING_ITEMS + page.items.count
        }

        page.showCursor(margin = margin)

        // A place just selected is the one shown, and the place remembered must not pull the list back.
        if (!state.isRevealing) {
            val place = memory?.of(path = path) ?: ScrollMemory.Place()

            list.scrollToItem(
                index = place.item,
                scrollOffset = place.offset
            )
        }

        launch {
            snapshotFlow { page.items.window.first }.drop(count = 1).collect {
                page.showCursor(margin = margin)
            }
        }

        if (memory != null) {
            snapshotFlow { page.place() }.collect {
                memory.keep(
                    path = path,
                    place = it
                )
            }
        }
    }
}

/**
 * Follows the view for the outline: the [tracker]'s current heading is the last one at or above the top of the view
 * (below the toolbar), or the first one while its block is in view below it. A heading the outline asks for is scrolled
 * to the top at once and stays the current one while the view stays there, even when the note ends too soon.
 */
@Composable
internal fun FollowOutline(
    state: EditorState,
    page: EditorPageState,
    tracker: OutlineTracker
) {
    val margin = with(LocalDensity.current) { HEADING_MARGIN_DP.dp.toPx() }
    val currentState = rememberUpdatedState(newValue = state)
    val revealed = remember(page) { mutableStateOf<Pair<Int, ScrollMemory.Place>?>(value = null) }

    LaunchedEffect(tracker, page) {
        tracker.onReveal { heading ->
            if (heading.offset > page.buffer.text.length) return@onReveal

            page.show(
                offset = heading.offset,
                top = page.toolbarHeight + margin
            )

            revealed.value = currentState
                .value
                .outline
                .indexOf(element = heading) to page.place()
        }
    }

    LaunchedEffect(tracker, page) {
        snapshotFlow {
            val outline = currentState.value.outline

            revealed
                .value
                ?.takeIf {
                    it.second == page.place() && it.first in outline.indices
                }
                ?.first
                ?: page.headingAt(
                    outline = outline,
                    line = page.toolbarHeight + margin + 1
                )
        }.collect { tracker.current = it }
    }
}

/**
 * Scrolls so the current match of [find] sits a little below the top of the editor, whenever it changes: to its line
 * inside the text field, to the top of its block when the block is drawn.
 */
@Composable
internal fun FollowMatch(
    page: EditorPageState,
    find: FindState?
) {
    val margin = with(LocalDensity.current) { MATCH_SCROLL_MARGIN_DP.dp.toPx() }
    val match = find?.current?.let { find.matches.getOrNull(index = it) }
    val isAnimated = !LocalReduceMotion.current

    LaunchedEffect(page, match) {
        if (match == null || match.first > page.buffer.text.length) return@LaunchedEffect

        page.show(
            offset = match.first,
            top = margin,
            isAnimated = isAnimated
        )
    }
}

private fun EditorPageState.place(): ScrollMemory.Place = ScrollMemory.Place(
    item = listState.firstVisibleItemIndex,
    offset = listState.firstVisibleItemScrollOffset
)

/**
 * The note's items that are in view below [top] pixels of the list. An item kept composed out of view (the text field)
 * is in the list's own count of visible items, so each one's place decides.
 */
private fun EditorPageState.seen(top: Float = 0f): List<LazyListItemInfo> {
    val info = listState.layoutInfo
    val blocks = LEADING_ITEMS until LEADING_ITEMS + items.count

    return info.visibleItemsInfo.filter {
        it.index in blocks &&
            it.offset < info.viewportEndOffset &&
            it.offset + it.size > info.viewportStartOffset + top
    }
}

/** Scrolls so the character at [offset] of the note is [top] pixels below the top of the list. */
private suspend fun EditorPageState.show(
    offset: Int,
    top: Float,
    isAnimated: Boolean = false
) {
    val item = items.itemOf(block = items.blocks.at(offset = offset))
    val index = LEADING_ITEMS + item
    val padding = listState.layoutInfo.beforeContentPadding

    // The text field out of view has no layout to tell where its lines are: it is brought in first.
    val isFieldOutOfView = items.isWindow(item = item) && currentLayout() == null

    if (isFieldOutOfView) {
        listState.scrollToItem(
            index = index,
            scrollOffset = (padding - top).roundToInt()
        )
    }

    val inItem = topIn(
        item = item,
        offset = offset
    )

    val scrollOffset = (inItem + padding - top).roundToInt()

    if (isAnimated) {
        listState.animateScrollToItem(
            index = index,
            scrollOffset = scrollOffset
        )
    } else {
        listState.scrollToItem(
            index = index,
            scrollOffset = scrollOffset
        )
    }
}

/**
 * Brings the text field into view when it is out of it: its first line at the bottom when it is the item right below
 * the view (the cursor walked down into it), else the cursor's line near the top, below the toolbar.
 */
private suspend fun EditorPageState.showCursor(margin: Float) {
    // What calls this follows state the list writes while it measures, and a list cannot be scrolled then.
    withFrameNanos {}

    val window = items.window
    val index = LEADING_ITEMS + window.first
    val seen = seen(top = toolbarHeight)

    if (seen.any { it.index == index }) return

    if (seen.lastOrNull()?.index == index - 1) {
        val info = listState.layoutInfo
        val offset = info.beforeContentPadding - info.viewportSize.height + margin

        listState.scrollToItem(
            index = index,
            scrollOffset = offset.roundToInt()
        )
    } else {
        show(
            offset = buffer.selection.start,
            top = toolbarHeight + margin
        )
    }
}

/**
 * The index of the heading of [outline] whose section is at [line] pixels below the top of the list: the last heading
 * at or above that line, else the first one once its block is in view, else [OutlineTracker.NONE]. A drawn block
 * counts whole, as only the text field knows where its lines are.
 */
private fun EditorPageState.headingAt(
    outline: List<OutlineHeading>,
    line: Float
): Int {
    val padding = listState.layoutInfo.beforeContentPadding
    val seen = seen()

    val above = seen.lastOrNull {
        it.offset + padding <= line
    }?.let { info ->
        val item = info.index - LEADING_ITEMS

        val limit = if (items.isWindow(item = item)) {
            val inField = currentLayout()?.let {
                val lineIndex = it.getLineForVerticalPosition(
                    vertical = line - info.offset - padding
                )

                it.getLineEnd(lineIndex = lineIndex)
            }

            inField?.let { shown.toNote(offset = it) } ?: shown.window.min
        } else {
            items.blocks.end(index = items.blockOf(item = item)) - 1
        }

        outline.indexOfLast {
            it.offset <= limit
        }
    }

    return above?.takeIf {
        it >= 0
    }
        ?: outline
            .firstOrNull()
            ?.takeIf { first ->
                val item =
                    LEADING_ITEMS + items.itemOf(block = items.blocks.at(offset = first.offset))

                first.offset <= buffer.text.length && seen.any { it.index == item }
            }
            ?.let { 0 }
        ?: OutlineTracker.NONE
}
