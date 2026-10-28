package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaSkeletonRow
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import kotlin.math.roundToInt

private const val PAGE_MAX_WIDTH_DP = 1100
private const val PAGE_MIN_WIDTH_DP = 760
private const val PAGE_SHARE = 0.7f
private val SKELETON_WIDTHS = listOf(0.9f, 0.75f, 0.85f, 0.6f, 0.8f, 0.4f)

/**
 * The page's width as the pane allows (`ROADMAP.md` 1.14): [PAGE_SHARE] of it, never narrower than [PAGE_MIN_WIDTH_DP]
 * nor wider than [PAGE_MAX_WIDTH_DP] (lines stay readable on a wide screen), and never wider than the pane.
 */
private fun Modifier.readableWidth(): Modifier = layout { measurable, constraints ->
    val width = (constraints.maxWidth * PAGE_SHARE).roundToInt()
        .coerceIn(minimumValue = PAGE_MIN_WIDTH_DP.dp.roundToPx(), maximumValue = PAGE_MAX_WIDTH_DP.dp.roundToPx())
        .coerceAtMost(maximumValue = constraints.maxWidth)
    val placeable = measurable.measure(constraints = constraints.copy(minWidth = width, maxWidth = width))
    layout(width = placeable.width, height = placeable.height) { placeable.place(x = 0, y = 0) }
}

/** The note's page, centred at a readable width: its header, the toolbar (kept in view) and its text or rendering. */
@Composable
internal fun EditorPage(
    state: EditorState,
    buffer: TextFieldState,
    scrollState: ScrollState,
    viewport: () -> LayoutCoordinates?,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    positions: HeadingPositions,
    header: EditorHeaderData,
    textFocusRequester: FocusRequester?,
    onToolbarHeight: (Int) -> Unit
) {
    var toolbarTop by remember { mutableIntStateOf(value = 0) }
    Column(
        modifier = Modifier
            .readableWidth()
            .padding(horizontal = NotekeaDimensions.SpacingXxl, vertical = NotekeaDimensions.SpacingXl)
    ) {
        EditorHeader(
            state = state,
            header = header,
            platformConventions = platformConventions,
            onIntent = onIntent
        )
        if (state.notePath != null) {
            Spacer(modifier = Modifier.height(height = NotekeaDimensions.SpacingM))
            EditorToolbar(
                isReading = state.isReading,
                platformConventions = platformConventions,
                onIntent = onIntent,
                modifier = Modifier
                    .onSizeChanged { onToolbarHeight(it.height) }
                    .stickyAtTop(scrollState = scrollState, viewport = viewport, top = {
                        toolbarTop
                    }) { toolbarTop = it }
            )
        }
        NoteText(
            state = state,
            buffer = buffer,
            scrollState = scrollState,
            platformConventions = platformConventions,
            onIntent = onIntent,
            positions = positions,
            textFocusRequester = textFocusRequester
        )
    }
}

/** The note's text with its Markdown highlighted, its rendering in Read mode, or a skeleton while it loads. */
@Composable
private fun NoteText(
    state: EditorState,
    buffer: TextFieldState,
    scrollState: ScrollState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    positions: HeadingPositions,
    textFocusRequester: FocusRequester?
) {
    if (state.isLoading) {
        EditorSkeleton()
    } else if (state.isReading) {
        ReadingView(
            text = buffer.text.toString(),
            attachments = state.attachments,
            onIntent = onIntent,
            onHeadingPlaced = { index, coordinates -> positions.inReading[index] = coordinates }
        )
    } else {
        EditorBody(
            buffer = buffer,
            scrollState = scrollState,
            platformConventions = platformConventions,
            onIntent = onIntent,
            attachments = state.attachments,
            outline = OutlinePlacement(headings = state.outline, positions = positions),
            find = state.find,
            focusRequester = textFocusRequester
        )
    }
}

/**
 * Keeps the toolbar in view: it scrolls with the note until it reaches the top of the editor, then stays there over
 * the text. [onTop] reports where it sits in the scrolled content (measured from the [viewport] that scrolls), which
 * [top] reads back while scrolling.
 */
private fun Modifier.stickyAtTop(
    scrollState: ScrollState,
    viewport: () -> LayoutCoordinates?,
    top: () -> Int,
    onTop: (Int) -> Unit
): Modifier = onPlaced { coordinates ->
    viewport()?.let { onTop((it.localPositionOf(sourceCoordinates = coordinates).y + scrollState.value).roundToInt()) }
}
    .zIndex(zIndex = 1f)
    .offset { IntOffset(x = 0, y = (scrollState.value - top()).coerceAtLeast(minimumValue = 0)) }

@Composable
private fun EditorSkeleton() {
    Column(
        modifier = Modifier.padding(top = NotekeaDimensions.SpacingL),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        SKELETON_WIDTHS.forEach { NotekeaSkeletonRow(widthFraction = it) }
    }
}

@Preview
@Composable
private fun EditorPagePreview() {
    NotekeaPreviewThemes {
        EditorPage(
            state = EditorPreviewData.state(),
            buffer = EditorPreviewData.buffer(),
            scrollState = rememberScrollState(),
            viewport = { null },
            platformConventions = EditorPreviewData.platformConventions,
            onIntent = {},
            positions = HeadingPositions { null },
            header = EditorHeaderData(dayNeighbors = null),
            textFocusRequester = null,
            onToolbarHeight = {}
        )
    }
}
