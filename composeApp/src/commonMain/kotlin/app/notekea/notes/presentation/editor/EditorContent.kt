package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import app.notekea.core.presentation.platform.LocalFileDrop
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.Canvas
import app.notekea.notes.domain.ListEditing
import app.notekea.notes.domain.ListAction
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.zIndex
import app.notekea.core.presentation.platform.matches
import app.notekea.core.presentation.platform.PressedModifiers
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaCheckbox
import app.notekea.core.presentation.components.NotekeaFindBar
import app.notekea.core.presentation.components.NotekeaMarkdown
import app.notekea.core.presentation.components.NotekeaSkeletonRow
import app.notekea.core.presentation.components.NotekeaTitleField
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.notekeaCodeTextStyle
import app.notekea.notes.domain.DayNeighbors
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.theme.NotekeaColors
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.editor_next_day
import app.notekea.resources.editor_previous_day
import androidx.compose.runtime.DisposableEffect
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.TaskFinder
import app.notekea.notes.presentation.tree.notesErrorMessage
import app.notekea.resources.Res
import app.notekea.resources.editor_bookmark_remove
import app.notekea.resources.editor_bookmark_add
import app.notekea.resources.editor_body_label
import app.notekea.resources.editor_find_count
import app.notekea.resources.editor_find_none
import app.notekea.resources.editor_saved
import app.notekea.resources.editor_task
import app.notekea.resources.editor_title_label
import app.notekea.resources.editor_unsaved
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private const val TASK_CHECKBOX_DP = 16
private const val TASK_CHECKBOX_RATIO = 0.8f
private const val BODY_MIN_HEIGHT_DP = 320
private const val CONTENT_MAX_WIDTH_DP = 760
private const val MATCH_SCROLL_MARGIN_DP = 96
private const val HEADING_MARGIN_DP = 8
private const val SCROLL_RESTORE_FRAMES = 30
internal const val NO_LIGATURES = "liga 0, calt 0"
private const val PATH_SEPARATOR = " / "
private const val PREVIEW_HEIGHT_DP = 520
private const val PREVIEW_WIDTH_DP = 760
private val SKELETON_WIDTHS = listOf(0.9f, 0.75f, 0.85f, 0.6f, 0.8f, 0.4f)

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
        val target = memory.of(path = path)
        repeat(times = SCROLL_RESTORE_FRAMES) {
            if (scrollState.maxValue >= target) return@repeat
            withFrameNanos { }
        }
        scrollState.scrollTo(value = target)
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

/** The note's page, centred at a readable width: its header, the toolbar (kept in view) and its text or rendering. */
@Composable
private fun EditorPage(
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
            .widthIn(max = CONTENT_MAX_WIDTH_DP.dp)
            .fillMaxWidth()
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

@Composable
private fun EditorHeader(
    state: EditorState,
    header: EditorHeaderData,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val path = state.notePath ?: return
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    path.parent?.takeUnless { it.isRoot }?.let { folder ->
        Text(
            text = folder.value.split(RelativePath.SEPARATOR).joinToString(separator = PATH_SEPARATOR),
            style = MaterialTheme.typography.bodySmall,
            color = muted
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        NotekeaTitleField(
            value = path.nameWithoutExtension,
            onCommit = { onIntent(EditorIntent.OnTitleCommit(name = it)) },
            label = stringResource(resource = Res.string.editor_title_label),
            modifier = Modifier.weight(weight = 1f),
            isError = state.titleError != null,
            onEdit = { if (state.titleError != null) onIntent(EditorIntent.OnTitleErrorDismiss) }
        )
        BookmarkStar(isBookmarked = header.isBookmarked, platformConventions = platformConventions, onIntent = onIntent)
        header.dayNeighbors?.let {
            DayArrows(neighbors = it, platformConventions = platformConventions, onIntent = onIntent)
        }
    }
    state.titleError?.let { error ->
        Text(
            text = notesErrorMessage(error = error, platformConventions = platformConventions),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    PropertiesView(state = state, onIntent = onIntent, modifier = Modifier.padding(top = NotekeaDimensions.SpacingXs))
}

/** The star beside the title: filled in yellow while the note is bookmarked (`ROADMAP.md` 1.13). */
@Composable
private fun BookmarkStar(
    isBookmarked: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    NotekeaIconButton(
        icon = if (isBookmarked) NotekeaIcons.Bookmarked else NotekeaIcons.Bookmark,
        label = stringResource(
            resource = if (isBookmarked) Res.string.editor_bookmark_remove else Res.string.editor_bookmark_add
        ),
        onClick = { onIntent(EditorIntent.OnBookmarkToggle) },
        shortcutHint = platformConventions.bookmarkShortcut.label(
            operatingSystem = platformConventions.operatingSystem
        ),
        takesFocus = false,
        tint = if (isBookmarked) NotekeaColors.bookmark else null
    )
}

/** A daily note's arrows to the daily notes before and after it, each greyed out when there is none on its side. */
@Composable
private fun DayArrows(
    neighbors: DayNeighbors,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem
    NotekeaIconButton(
        icon = NotekeaIcons.PreviousDay,
        label = stringResource(resource = Res.string.editor_previous_day),
        onClick = { onIntent(EditorIntent.OnDayStep(step = -1)) },
        enabled = neighbors.previous != null,
        shortcutHint = platformConventions.previousDayShortcut.label(operatingSystem = operatingSystem),
        takesFocus = false
    )
    NotekeaIconButton(
        icon = NotekeaIcons.NextDay,
        label = stringResource(resource = Res.string.editor_next_day),
        onClick = { onIntent(EditorIntent.OnDayStep(step = 1)) },
        enabled = neighbors.next != null,
        shortcutHint = platformConventions.nextDayShortcut.label(operatingSystem = operatingSystem),
        takesFocus = false
    )
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

/** Markdown highlighting plus the find matches, which are read inside the transformation so they restyle at once. */
@Composable
private fun rememberHighlighting(
    find: FindState?,
    paragraphs: (String) -> List<Pair<IntRange, ParagraphStyle>>
): MarkdownOutputTransformation {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val codeFontFamily = notekeaCodeTextStyle().fontFamily
    val currentFind = rememberUpdatedState(newValue = find)
    return remember(colors, typography, codeFontFamily) {
        MarkdownOutputTransformation(
            styles = MarkdownStyles.from(
                colorScheme = colors,
                typography = typography,
                codeFontFamily = codeFontFamily
            ),
            findState = { currentFind.value },
            paragraphs = paragraphs
        )
    }
}

@Composable
private fun EditorBody(
    buffer: TextFieldState,
    scrollState: ScrollState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    attachments: List<AttachmentView>,
    outline: OutlinePlacement,
    find: FindState?,
    focusRequester: FocusRequester?
) {
    val colors = MaterialTheme.colorScheme
    val room = AttachmentRoom.remember(attachments = attachments)
    val highlighting = rememberHighlighting(find = find, paragraphs = room.paragraphs)
    var hasFocus by remember { mutableStateOf(value = false) }
    var layout by remember { mutableStateOf<() -> TextLayoutResult?>(value = { null }) }
    var textPlace by remember { mutableStateOf<LayoutCoordinates?>(value = null) }
    val label = stringResource(resource = Res.string.editor_body_label)
    val onKey = rememberEditorKeys(buffer = buffer, platformConventions = platformConventions, onIntent = onIntent)
    val top = { textPlace?.takeIf { it.isAttached }?.positionInParent()?.y ?: 0f }
    ScrollToCurrentMatch(scrollState = scrollState, layout = { layout() }, top = top, find = find)
    PlaceHeadings(outline = outline, layout = { layout() }, textPlace = { textPlace })
    BasicTextField(
        state = buffer,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { textPlace = it }
            // A short note still leaves room to click below its last line and keep typing.
            .defaultMinSize(minHeight = BODY_MIN_HEIGHT_DP.dp)
            .padding(top = NotekeaDimensions.SpacingL)
            .semantics { contentDescription = label }
            .then(other = focusRequester?.let { Modifier.focusRequester(focusRequester = it) } ?: Modifier)
            .onPreviewKeyEvent(onPreviewKeyEvent = onKey)
            .onFocusChanged {
                if (hasFocus && !it.isFocused) onIntent(EditorIntent.OnFocusLost)
                hasFocus = it.isFocused
            },
        // Ligatures would turn what is typed into other glyphs (`->` into an arrow): the editor shows the characters.
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = colors.onSurface,
            fontFeatureSettings = NO_LIGATURES
        ),
        cursorBrush = SolidColor(value = colors.primary),
        onTextLayout = { getResult -> layout = getResult },
        outputTransformation = highlighting,
        decorator = decoratorOf(buffer = buffer, layout = { layout() }, onIntent = onIntent) {
            room.Overlay(buffer = buffer, attachments = attachments, layout = { layout() }, onIntent = onIntent)
        }
    )
}

/** The toolbar action of a formatting shortcut pressed in the text (bold, italic, links), or null for any other key. */
private fun formatShortcut(
    event: KeyEvent,
    platformConventions: PlatformConventions,
    formats: FormatActions
): EditorIntent? = when {
    platformConventions.boldShortcut.matches(event = event) -> EditorIntent.OnFormat(action = formats.bold)

    platformConventions.italicShortcut.matches(event = event) -> EditorIntent.OnFormat(action = formats.italic)

    platformConventions.noteLinkShortcut.matches(event = event) -> EditorIntent.OnNoteLinkRequest

    platformConventions.webLinkShortcut.matches(event = event) -> EditorIntent.OnFormat(action = formats.webLink)

    // A copied image is attached; copied text is pasted by the editor too, so both behave the same.
    platformConventions.pasteShortcut.matches(event = event) -> EditorIntent.OnPaste

    else -> null
}

/**
 * The keys pressed in the text that the editor handles itself, the formatting shortcuts and the list keys, sent as
 * intents; it returns false for the text field to handle every other key.
 */
@Composable
private fun rememberEditorKeys(
    buffer: TextFieldState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
): (KeyEvent) -> Boolean {
    val formats = formatActions()
    val listEditing = remember { ListEditing() }
    return { event ->
        val intent = formatShortcut(event = event, platformConventions = platformConventions, formats = formats)
            ?: listKey(event = event, buffer = buffer, listEditing = listEditing)
        intent?.let { onIntent(it) } != null
    }
}

/**
 * Enter, Tab, Shift+Tab or Backspace where they edit a list (`ListEditing`); null where the key types as usual, so the
 * text field handles it.
 */
private fun listKey(
    event: KeyEvent,
    buffer: TextFieldState,
    listEditing: ListEditing
): EditorIntent? {
    if (event.type != KeyEventType.KeyDown || event.isCtrlPressed || event.isMetaPressed || event.isAltPressed) {
        return null
    }
    val action = when (event.key) {
        Key.Enter, Key.NumPadEnter -> ListAction.NEW_ITEM.takeUnless { event.isShiftPressed }
        Key.Tab -> if (event.isShiftPressed) ListAction.OUTDENT else ListAction.INDENT
        Key.Backspace -> ListAction.REMOVE_MARKER.takeUnless { event.isShiftPressed }
        else -> null
    } ?: return null
    val selection = buffer.selection
    val edit = listEditing.edit(
        text = buffer.text.toString(),
        selectionStart = selection.start,
        selectionEnd = selection.end,
        action = action
    )
    return edit?.let { EditorIntent.OnListKey(action = action) }
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

/**
 * A clickable checkbox over the `[ ]` / `[x]` of every task of the text (`ROADMAP.md` 1.7), placed with the text's
 * layout, so tasks can be checked in Edit mode as in Read mode; a click toggles the character in the file. The box
 * covers the brackets with the editor's background. It recomposes on every keystroke, which only moves these boxes.
 */
@Composable
private fun TaskCheckboxes(
    buffer: TextFieldState,
    layout: () -> TextLayoutResult?,
    onIntent: (EditorIntent) -> Unit
) {
    val taskFinder = remember { TaskFinder() }
    val text = buffer.text
    val tasks = remember(text) { taskFinder.tasks(text = text.toString()) }
    val result = layout() ?: return
    val density = LocalDensity.current
    val label = stringResource(resource = Res.string.editor_task)
    val background = MaterialTheme.colorScheme.surface
    tasks.filter { it.stateIndex + 1 < result.layoutInput.text.length }.forEach { task ->
        val open = result.getBoundingBox(offset = task.stateIndex - 1)
        val close = result.getBoundingBox(offset = task.stateIndex + 1)
        val width = with(density) { (close.right - open.left).toDp() }
        val height = with(density) { (open.bottom - open.top).toDp() }
        key(task.stateIndex) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = open.left.roundToInt(), y = open.top.roundToInt()) }
                    .size(width = width, height = height)
                    .background(color = background),
                contentAlignment = Alignment.Center
            ) {
                NotekeaCheckbox(
                    checked = task.isDone,
                    onClick = { onIntent(EditorIntent.OnTaskToggle(stateIndex = task.stateIndex)) },
                    label = label,
                    size = minOf(TASK_CHECKBOX_DP, (height.value * TASK_CHECKBOX_RATIO).roundToInt())
                )
            }
        }
    }
}

/** The code blocks' backgrounds behind the text, and the tasks' checkboxes over it. */
private fun decoratorOf(
    buffer: TextFieldState,
    layout: () -> TextLayoutResult?,
    onIntent: (EditorIntent) -> Unit,
    overlay: @Composable () -> Unit
): TextFieldDecorator = TextFieldDecorator { innerTextField ->
    Box {
        CodeBlockBackgrounds(buffer = buffer, layout = layout)
        innerTextField()
        TaskCheckboxes(buffer = buffer, layout = layout, onIntent = onIntent)
        overlay()
    }
}

/**
 * The background of every fenced code block, behind its whole lines from one fence to the other and a little beyond
 * the text on each side, as a block: shading only the characters left a ragged edge and hid the spaces ending a line.
 */
@Composable
private fun BoxScope.CodeBlockBackgrounds(
    buffer: TextFieldState,
    layout: () -> TextLayoutResult?
) {
    val highlighter = remember { MarkdownHighlighter() }
    val text = buffer.text
    val blocks = remember(text) { highlighter.codeBlocks(text = text) }
    val color = MaterialTheme.colorScheme.surfaceVariant
    val padding = with(LocalDensity.current) { NotekeaDimensions.SpacingXs.toPx() }
    val radius = with(LocalDensity.current) { NotekeaDimensions.ControlRadius.toPx() }
    Canvas(modifier = Modifier.matchParentSize()) {
        val result = layout() ?: return@Canvas
        val length = result.layoutInput.text.length
        blocks.filter { it.first <= length }.forEach { block ->
            val top = result.getLineTop(lineIndex = result.getLineForOffset(offset = block.first))
            val bottom = result.getLineBottom(
                lineIndex = result.getLineForOffset(offset = minOf(block.last + 1, length))
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x = -padding, y = top),
                size = Size(width = size.width + 2 * padding, height = bottom - top),
                cornerRadius = CornerRadius(x = radius, y = radius)
            )
        }
    }
}

/**
 * Tells [OutlinePlacement.positions] where each heading of the text is, from its layout: the text's place in the page
 * ([textPlace], above its top padding), plus the heading's line.
 */
@Composable
private fun PlaceHeadings(
    outline: OutlinePlacement,
    layout: () -> TextLayoutResult?,
    textPlace: () -> LayoutCoordinates?
) {
    val padding = with(LocalDensity.current) { NotekeaDimensions.SpacingL.toPx() }
    val headings by rememberUpdatedState(newValue = outline.headings)
    DisposableEffect(outline.positions) {
        outline.positions.inText = { index ->
            val result = layout()
            val offset = headings.getOrNull(index = index)?.offset
            val textTop = textPlace()?.let { outline.positions.pageTopOf(coordinates = it) }
            if (result == null || offset == null || textTop == null || offset > result.layoutInput.text.length) {
                null
            } else {
                textTop + padding + result.getLineTop(lineIndex = result.getLineForOffset(offset = offset))
            }
        }
        onDispose { outline.positions.inText = { null } }
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

/** Scrolls so the current match sits a little below the top of the editor, whenever it changes. */
@Composable
private fun ScrollToCurrentMatch(
    scrollState: ScrollState,
    layout: () -> TextLayoutResult?,
    top: () -> Float,
    find: FindState?
) {
    val margin = with(LocalDensity.current) { MATCH_SCROLL_MARGIN_DP.dp.toPx() }
    val match = find?.current?.let { find.matches.getOrNull(index = it) }
    LaunchedEffect(match) {
        if (match == null) return@LaunchedEffect
        // The text may not be laid out yet (Find opened with the note): wait for its first layout.
        val result = snapshotFlow { layout() }.filterNotNull().first()
        if (match.first >= result.layoutInput.text.length) return@LaunchedEffect
        val lineTop = result.getLineTop(lineIndex = result.getLineForOffset(offset = match.first))
        scrollState.animateScrollTo(value = (top() + lineTop - margin).roundToInt().coerceAtLeast(minimumValue = 0))
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
