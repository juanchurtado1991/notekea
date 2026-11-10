package app.notekea.editor.presentation.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaSkeletonRow
import app.notekea.core.presentation.designsystem.components.TextMarks
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.find.NoteSearch
import app.notekea.editor.presentation.markdown.MarkdownStyles
import app.notekea.editor.presentation.model.EditorHeaderData
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.toolbar.EditorToolbar
import app.notekea.editor.presentation.view.EditorPageState.Companion.WINDOW_KEY
import kotlin.math.roundToInt

private const val END_KEY = "end"
private const val END_ROOM_DP = 320
private const val HEADER_KEY = "header"
private const val PAGE_MAX_WIDTH_DP = 1100
private const val PAGE_MIN_WIDTH_DP = 760
private const val PAGE_SHARE = 0.7f
private const val PREVIEW_HEIGHT_DP = 520
private const val PREVIEW_WIDTH_DP = 760
private const val SKELETON_KEY = "skeleton"
private val SKELETON_WIDTHS = listOf(0.9f, 0.75f, 0.85f, 0.6f, 0.8f, 0.4f)
internal const val TOOLBAR_KEY = "toolbar"

/**
 * The page's width as the pane allows: [PAGE_SHARE] of it, never narrower than [PAGE_MIN_WIDTH_DP]
 * nor wider than [PAGE_MAX_WIDTH_DP] (lines stay readable on a wide screen), and never wider than the pane.
 */
private fun Modifier.readableWidth(): Modifier = layout { measurable, constraints ->
    val width = (constraints.maxWidth * PAGE_SHARE)
        .roundToInt()
        .coerceIn(
            minimumValue = PAGE_MIN_WIDTH_DP.dp.roundToPx(),
            maximumValue = PAGE_MAX_WIDTH_DP.dp.roundToPx()
        )
        .coerceAtMost(maximumValue = constraints.maxWidth)

    val placeable = measurable.measure(
        constraints = constraints.copy(
            minWidth = width,
            maxWidth = width
        )
    )

    layout(
        width = placeable.width,
        height = placeable.height
    ) {
        placeable.place(
            x = 0,
            y = 0
        )
    }
}

/**
 * Find's matches for the blocks drawn as Read mode draws them, or null while Find is closed. The query is read when a
 * block is painted, so typing it paints the blocks again without laying them out.
 */
@Composable
private fun rememberFindMarks(find: FindState?): TextMarks? {
    val color = MarkdownStyles.findMatchColor(colorScheme = MaterialTheme.colorScheme)
    val query = rememberUpdatedState(newValue = find?.query.orEmpty())
    val search = remember { NoteSearch() }

    val marks = remember(color) {
        TextMarks(color = color) {
            search.matches(
                text = it,
                query = query.value
            )
        }
    }

    return marks.takeIf {
        find != null
    }
}

/**
 * The note's page as a list, so only what is in view is composed: its header, the sticky toolbar, then the note's
 * blocks, drawn as Read mode draws them except the ones the cursor is in, which are the text field ([EditorWindow]).
 * A block has no identity of its own, so its place in the note is its key.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EditorList(
    state: EditorState,
    page: EditorPageState,
    header: EditorHeaderData,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultBringIntoView = LocalBringIntoViewSpec.current
    val marks = rememberFindMarks(find = state.find)

    CompositionLocalProvider(LocalBringIntoViewSpec provides EditorBringIntoViewSpec) {
        LazyColumn(
            state = page.listState,
            contentPadding = PaddingValues(vertical = NotekeaDimensions.SpacingXl),
            modifier = modifier
                .fillMaxSize()
                .onPlaced { page.listPlace = it }
                .selectAcrossBlocks(page = page)
        ) {
            item(
                key = HEADER_KEY,
                contentType = HEADER_KEY
            ) {
                PageItem(bringIntoView = defaultBringIntoView) {
                    EditorHeader(
                        state = state,
                        header = header,
                        platformConventions = platformConventions,
                        onIntent = onIntent
                    )

                    Spacer(modifier = Modifier.height(height = NotekeaDimensions.SpacingM))
                }
            }

            stickyHeader(
                key = TOOLBAR_KEY,
                contentType = TOOLBAR_KEY
            ) {
                PageItem(bringIntoView = defaultBringIntoView) {
                    if (state.notePath != null) {
                        EditorToolbar(
                            isReading = state.isReading,
                            isSource = state.isSourceMode,
                            platformConventions = platformConventions,
                            onIntent = onIntent,
                            modifier = Modifier.onSizeChanged {
                                page.toolbarHeight = it.height.toFloat()
                            }
                        )
                    }
                }
            }

            if (state.isLoading) {
                item(
                    key = SKELETON_KEY,
                    contentType = SKELETON_KEY
                ) {
                    PageItem(bringIntoView = defaultBringIntoView) { EditorSkeleton() }
                }
            } else {
                blocks(
                    state = state,
                    page = page,
                    bringIntoView = defaultBringIntoView,
                    platformConventions = platformConventions,
                    onIntent = onIntent,
                    marks = marks
                )
            }
        }
    }
}

/** The note's blocks, then room below its last line, where a click puts the cursor at the note's end. */
private fun LazyListScope.blocks(
    state: EditorState,
    page: EditorPageState,
    bringIntoView: BringIntoViewSpec,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    marks: TextMarks?
) {
    val items = page.items

    items(
        count = items.count,
        key = {
            if (items.isWindow(item = it)) WINDOW_KEY else items.blockOf(item = it)
        },
        contentType = { items.isWindow(item = it) }
    ) { item ->
        val block = items.blockOf(item = item)
        val top = if (item == 0) NotekeaDimensions.SpacingL else 0.dp

        PageItem(
            bringIntoView = bringIntoView,
            modifier = Modifier.padding(top = top)
        ) {
            if (items.isWindow(item = item)) {
                EditorWindow(
                    page = page,
                    find = state.find,
                    platformConventions = platformConventions,
                    onIntent = onIntent
                )

                // Source is the note as written: no pictures nor query tables under it.
                if (!state.isSourceMode) {
                    WindowEmbeds(
                        page = page,
                        attachments = state.attachments,
                        onIntent = onIntent
                    )
                }
            } else {
                EditorBlock(
                    text = items.textOf(
                        text = page.buffer.text,
                        block = block
                    ),
                    textStart = items.blocks.start(index = block),
                    attachments = state.attachments,
                    isReading = state.isReading,
                    onIntent = onIntent,
                    onClick = { position, drawnHeight, isExtending ->
                        page.click(
                            block = block,
                            position = position,
                            drawnHeight = drawnHeight,
                            isExtending = isExtending
                        )
                    },
                    marks = marks
                )
            }
        }
    }

    if (state.isEditable) {
        item(
            key = END_KEY,
            contentType = END_KEY
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = END_ROOM_DP.dp)
                    .pointerHoverIcon(icon = PointerIcon.Text)
                    .pointerInput(page) {
                        detectTapGestures { page.clickAfterEnd() }
                    }
            )
        }
    }
}

/** One item of the page, centred at a readable width; what scrolls inside it does so as usual ([bringIntoView]). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PageItem(
    bringIntoView: BringIntoViewSpec,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val column = Modifier
            .readableWidth()
            .padding(horizontal = NotekeaDimensions.SpacingXxl)

        Column(modifier = column) {
            CompositionLocalProvider(LocalBringIntoViewSpec provides bringIntoView) { content() }
        }
    }
}

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
private fun EditorListPreview() {
    val buffer = remember { EditorPreviewData.buffer() }
    val listState = rememberLazyListState()

    val page = remember(buffer, listState) {
        EditorPageState(
            buffer = buffer,
            listState = listState,
            focusRequester = FocusRequester(),
            isReading = { false },
            isSource = { false }
        )
    }

    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            EditorList(
                state = EditorPreviewData.state(),
                page = page,
                header = EditorHeaderData(dayNeighbors = null),
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}
