package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaFindBar
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.presentation.platform.PressedModifiers
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.platform.matches
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.NotesError
import app.notekea.resources.Res
import app.notekea.resources.editor_find_count
import app.notekea.resources.editor_find_none
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private const val HEADING_MARGIN_DP = 8
internal const val NO_LIGATURES = "liga 0, calt 0"
private const val PREVIEW_HEIGHT_DP = 520
private const val PREVIEW_WIDTH_DP = 760

/**
 * The note editor (`ROADMAP.md` 1.5): the questions that need an answer on top (outside change, deleted note, failed
 * save), then the note's folder, its title (editable: it renames the note), whether it is saved, its tags and aliases
 * from the frontmatter (read-only), and the body with its Markdown highlighted. The text is centred and limited to a
 * readable width; the header scrolls with it. Leaving the text saves at once. Find in note shows its bar above the
 * text, marks the matches and scrolls to the current one. Read mode shows the note rendered instead of its Markdown.
 * The [outlineTracker] learns which heading the view is in and scrolls to the headings the outline asks for. Files
 * dropped anywhere on the editor are attached at the cursor, in Edit mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorContent(
    state: EditorState,
    buffer: TextFieldState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    textFocusRequester: FocusRequester? = null,
    outlineTracker: OutlineTracker? = null,
    dayNeighbors: DayNeighbors? = null,
    isBookmarked: Boolean = false,
    scrollMemory: ScrollMemory? = null
) {
    val scrollState = rememberScrollState()
    scrollMemory?.let { RememberScroll(state = state, scrollState = scrollState, memory = it) }
    val pressed = remember { PressedModifiers() }
    // Read only when the toolbar is placed, never in composition, so updating it recomposes nothing.
    val viewport = remember { mutableStateOf<LayoutCoordinates?>(value = null) }
    val positions = rememberHeadingPositions(viewport = viewport, scrollState = scrollState)
    val keepInView = remember { EditorBringIntoViewSpec() }
    val fileDrop = LocalFileDrop.current { paths -> onIntent(EditorIntent.OnFilesDrop(paths = paths)) }
    val tracker = outlineTracker ?: remember { OutlineTracker() }
    FollowOutline(
        state = state,
        tracker = tracker,
        positions = positions,
        scrollState = scrollState,
        toolbarHeight = { keepInView.topInset }
    )
    val header = EditorHeaderData(dayNeighbors = dayNeighbors, isBookmarked = isBookmarked)
    val surface = modifier.fillMaxSize().then(other = fileDrop).then(other = pressed.tracking())
    Surface(modifier = surface, color = MaterialTheme.colorScheme.surface) {
        Column {
            EditorBanners(state = state, platformConventions = platformConventions, onIntent = onIntent)
            state.find?.let { find -> EditorFindBar(find = find, onIntent = onIntent) }
            ScrollingPage(scrollState = scrollState, viewport = viewport, keepInView = keepInView) {
                EditorPage(
                    state = state,
                    buffer = buffer,
                    scrollState = scrollState,
                    viewport = { viewport.value },
                    platformConventions = platformConventions,
                    onIntent = withModClicks(pressed = pressed, onIntent = onIntent),
                    positions = positions,
                    header = header,
                    textFocusRequester = textFocusRequester,
                    onToolbarHeight = { keepInView.topInset = it.toFloat() }
                )
            }
        }
    }
}

/**
 * [onIntent], with a link clicked while `Mod` was held ([pressed]) opening behind the current tab (`ROADMAP.md` 1.11):
 * the Markdown renderer only says which link was clicked, not how.
 */
private fun withModClicks(
    pressed: PressedModifiers,
    onIntent: (EditorIntent) -> Unit
): (EditorIntent) -> Unit = { intent ->
    onIntent(if (intent is EditorIntent.OnLinkClick) intent.copy(inBackground = pressed.isModPressed) else intent)
}

/**
 * Keeps where [state]'s note is scrolled to in [memory] while it is shown, and scrolls back there when it is shown
 * again (`ROADMAP.md` 1.11), once its text is laid out far enough to get there.
 */
@Composable
private fun RememberScroll(
    state: EditorState,
    scrollState: ScrollState,
    memory: ScrollMemory
) {
    val path = state.notePath
    LaunchedEffect(path, state.isLoading, state.isReading) {
        if (path == null || state.isLoading) return@LaunchedEffect
        scrollState.scrollWhenLaidOut(target = memory.of(path = path))
        snapshotFlow { scrollState.value }.collect { memory.keep(path = path, offset = it) }
    }
}

/** Where the outline's headings are, measured from the [viewport], in which the page scrolls by [scrollState]. */
@Composable
private fun rememberHeadingPositions(
    viewport: State<LayoutCoordinates?>,
    scrollState: ScrollState
): HeadingPositions = remember(viewport, scrollState) {
    HeadingPositions { coordinates ->
        viewport.value?.takeIf { it.isAttached }?.let {
            it.localPositionOf(sourceCoordinates = coordinates).y + scrollState.value
        }
    }
}

/**
 * The [page] scrolled in the editor's viewport (reported to [viewport]). Only the editor's scroll keeps things below the
 * toolbar ([keepInView]); the fields inside scroll as usual.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollingPage(
    scrollState: ScrollState,
    viewport: MutableState<LayoutCoordinates?>,
    keepInView: EditorBringIntoViewSpec,
    page: @Composable () -> Unit
) {
    val defaultBringIntoView = LocalBringIntoViewSpec.current
    CompositionLocalProvider(LocalBringIntoViewSpec provides keepInView) {
        Box(
            modifier = Modifier.fillMaxSize().onPlaced { viewport.value = it }.verticalScroll(state = scrollState),
            contentAlignment = Alignment.TopCenter
        ) {
            CompositionLocalProvider(LocalBringIntoViewSpec provides defaultBringIntoView) { page() }
        }
    }
}

@Composable
private fun EditorFindBar(
    find: FindState,
    onIntent: (EditorIntent) -> Unit
) {
    val current = find.current
    val label = when {
        find.query.isEmpty() -> ""
        current == null -> stringResource(resource = Res.string.editor_find_none)
        else -> stringResource(Res.string.editor_find_count, current + 1, find.matches.size)
    }
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = NotekeaDimensions.SpacingM)) {
        NotekeaFindBar(
            query = find.query,
            onQueryChange = { onIntent(EditorIntent.OnFindQueryChange(query = it)) },
            matchLabel = label,
            onNext = { onIntent(EditorIntent.OnFindNext) },
            onPrevious = { onIntent(EditorIntent.OnFindPrevious) },
            onClose = { onIntent(EditorIntent.OnFindClose) },
            modifier = Modifier.align(alignment = Alignment.CenterEnd)
        )
    }
}

/**
 * Follows the view for the outline: the [tracker]'s current heading is the last one at or above the top of the view
 * (below the toolbar), or the first one while it is in view below it, and a heading the outline asks for is scrolled to
 * the top of the view at once. A heading just revealed
 * stays the current one while the view stays where it put it, even when the note ends too soon to bring it up there.
 */
@Composable
private fun FollowOutline(
    state: EditorState,
    tracker: OutlineTracker,
    positions: HeadingPositions,
    scrollState: ScrollState,
    toolbarHeight: () -> Float
) {
    val margin = with(LocalDensity.current) { HEADING_MARGIN_DP.dp.toPx() }
    val currentState = rememberUpdatedState(newValue = state)
    val revealed = remember { mutableStateOf<Pair<Int, Int>?>(value = null) }
    LaunchedEffect(tracker) {
        tracker.onReveal { heading ->
            val index = currentState.value.outline.indexOf(element = heading)
            val top = positions.top(index = index, isReading = currentState.value.isReading) ?: return@onReveal
            scrollState.scrollTo(value = (top - toolbarHeight() - margin).roundToInt().coerceAtLeast(minimumValue = 0))
            revealed.value = index to scrollState.value
        }
    }
    LaunchedEffect(tracker) {
        snapshotFlow {
            val shown = currentState.value
            val line = scrollState.value + toolbarHeight() + margin + 1
            val top = { index: Int -> positions.top(index = index, isReading = shown.isReading) }
            revealed.value?.takeIf { it.second == scrollState.value && it.first in shown.outline.indices }?.first
                ?: shown.outline.indices.lastOrNull { index -> top(index)?.let { it <= line } == true }
                // Above the first heading, it is the current one once it can be seen.
                ?: shown.outline.indices.firstOrNull()?.takeIf { index ->
                    top(index)?.let { it < scrollState.value + scrollState.viewportSize } == true
                }
                ?: OutlineTracker.NONE
        }.collect { tracker.current = it }
    }
}

@Composable
private fun EditorPreviewFrame(
    state: EditorState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            EditorContent(
                state = state,
                buffer = EditorPreviewData.buffer(),
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}

@Preview
@Composable
private fun EditorContentPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state())
}

@Preview
@Composable
private fun EditorContentUnsavedPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(saveStatus = SaveStatus.UNSAVED))
}

@Preview
@Composable
private fun EditorContentFindPreview() {
    EditorPreviewFrame(
        state = EditorPreviewData.state().copy(
            find = FindState(query = "corn", matches = listOf(EditorPreviewData.CORN_MATCH), current = 0)
        )
    )
}

@Preview
@Composable
private fun EditorContentReadingPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(isReading = true))
}

@Preview
@Composable
private fun EditorContentLoadingPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(isLoading = true))
}

@Preview
@Composable
private fun EditorContentConflictPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(hasConflict = true))
}

@Preview
@Composable
private fun EditorContentDeletedPreview() {
    EditorPreviewFrame(state = EditorPreviewData.state().copy(isDeleted = true))
}

@Preview
@Composable
private fun EditorContentErrorsPreview() {
    EditorPreviewFrame(
        state = EditorPreviewData.state().copy(
            saveError = NotesError.OperationFailed(path = EditorPreviewData.path.value),
            titleError = NotesError.NameTaken(name = "Pasta e fagioli.md")
        )
    )
}

@Preview
@Composable
private fun EditorContentLoadErrorPreview() {
    EditorPreviewFrame(
        state = EditorPreviewData.state().copy(
            loadError = NotesError.OperationFailed(path = EditorPreviewData.path.value)
        )
    )
}
