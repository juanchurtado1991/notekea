package app.notekea.notes.presentation.editor

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import app.notekea.core.presentation.platform.LocalFileDrop
import app.notekea.core.domain.StringPatterns.SPACE
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.TaskFinder
import app.notekea.notes.presentation.tree.notesErrorMessage
import app.notekea.resources.Res
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
internal const val NO_LIGATURES = "liga 0, calt 0"
private const val PATH_SEPARATOR = " / "
private const val PREVIEW_HEIGHT_DP = 520
private const val PREVIEW_WIDTH_DP = 760
private const val PROPERTY_SEPARATOR = " · "
private const val SAVED_VISIBLE_MILLIS = 1_000L
private const val TAG_PREFIX = "#"
private val SKELETON_WIDTHS = listOf(0.9f, 0.75f, 0.85f, 0.6f, 0.8f, 0.4f)

/**
 * The note editor (`ROADMAP.md` 1.5): the questions that need an answer on top (outside change, deleted note, failed
 * save), then the note's folder, its title (editable: it renames the note), whether it is saved, its tags and aliases
 * from the frontmatter (read-only), and the body with its Markdown highlighted. The text is centred and limited to a
 * readable width; the header scrolls with it. Leaving the text saves at once. Find in note shows its bar above the
 * text, marks the matches and scrolls to the current one. Read mode shows the note rendered instead of its Markdown.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorContent(
    state: EditorState,
    buffer: TextFieldState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier,
    properties: NoteMetadata? = null,
    textFocusRequester: FocusRequester? = null
) {
    val scrollState = rememberScrollState()
    // Read only when the toolbar is placed, never in composition, so updating it recomposes nothing.
    val viewport = remember { mutableStateOf<LayoutCoordinates?>(value = null) }
    val keepInView = remember { EditorBringIntoViewSpec() }
    val defaultBringIntoView = LocalBringIntoViewSpec.current
    // Files dropped anywhere on the editor are attached at the cursor, in Edit mode.
    val fileDrop = LocalFileDrop.current { paths -> onIntent(EditorIntent.OnFilesDrop(paths = paths)) }
    Surface(modifier = modifier.fillMaxSize().then(other = fileDrop), color = MaterialTheme.colorScheme.surface) {
        Column {
            EditorBanners(state = state, platformConventions = platformConventions, onIntent = onIntent)
            state.find?.let { find -> EditorFindBar(find = find, onIntent = onIntent) }
            CompositionLocalProvider(LocalBringIntoViewSpec provides keepInView) {
                Box(
                    modifier = Modifier.fillMaxSize().onPlaced {
                        viewport.value = it
                    }.verticalScroll(state = scrollState),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Only the editor's scroll keeps things below the toolbar; the fields inside scroll as usual.
                    CompositionLocalProvider(LocalBringIntoViewSpec provides defaultBringIntoView) {
                        EditorPage(
                            state = state,
                            buffer = buffer,
                            scrollState = scrollState,
                            viewport = { viewport.value },
                            platformConventions = platformConventions,
                            onIntent = onIntent,
                            properties = properties,
                            textFocusRequester = textFocusRequester,
                            onToolbarHeight = { keepInView.topInset = it.toFloat() }
                        )
                    }
                }
            }
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
    properties: NoteMetadata?,
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
            properties = properties,
            platformConventions = platformConventions,
            onIntent = onIntent
        )
        if (state.notePath != null) {
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
    textFocusRequester: FocusRequester?
) {
    if (state.isLoading) {
        EditorSkeleton()
    } else if (state.isReading) {
        ReadingView(text = buffer.text.toString(), attachments = state.attachments, onIntent = onIntent)
    } else {
        EditorBody(
            buffer = buffer,
            scrollState = scrollState,
            platformConventions = platformConventions,
            onIntent = onIntent,
            attachments = state.attachments,
            find = state.find,
            focusRequester = textFocusRequester
        )
    }
}

@Composable
private fun EditorHeader(
    state: EditorState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    properties: NoteMetadata?
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
        SaveIndicator(status = state.saveStatus)
    }
    state.titleError?.let { error ->
        Text(
            text = notesErrorMessage(error = error, platformConventions = platformConventions),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
    propertiesLine(properties = properties)?.let { line ->
        Text(text = line, style = MaterialTheme.typography.bodySmall, color = muted)
    }
}

/** Tags and aliases on one line (`#recipe #venezuelan · Arepa`), or null when the note has neither. */
private fun propertiesLine(
    properties: NoteMetadata?
): String? {
    val tags = properties?.tags.orEmpty().joinToString(separator = SPACE) { TAG_PREFIX + it }
    val aliases = properties?.aliases.orEmpty().joinToString(separator = PROPERTY_SEPARATOR)
    return listOf(tags, aliases).filter { it.isNotEmpty() }.joinToString(separator = PROPERTY_SEPARATOR).ifEmpty {
        null
    }
}

/** "Unsaved" while edits wait for the autosave; "Saved" for a second after a save, then nothing. */
@Composable
private fun SaveIndicator(
    status: SaveStatus
) {
    var isSavedShown by remember { mutableStateOf(value = false) }
    LaunchedEffect(status) {
        isSavedShown = status == SaveStatus.SAVED
        if (isSavedShown) {
            delay(timeMillis = SAVED_VISIBLE_MILLIS)
            isSavedShown = false
        }
    }
    val text = when {
        status == SaveStatus.UNSAVED -> stringResource(resource = Res.string.editor_unsaved)
        isSavedShown -> stringResource(resource = Res.string.editor_saved)
        else -> null
    }
    AnimatedVisibility(
        visible = text != null,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = NotekeaDimensions.MOTION_PANEL_MILLIS))
    ) {
        Text(
            text = text.orEmpty(),
            modifier = Modifier.padding(start = NotekeaDimensions.SpacingM),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    find: FindState?,
    focusRequester: FocusRequester?
) {
    val colors = MaterialTheme.colorScheme
    val room = AttachmentRoom.remember(attachments = attachments)
    val highlighting = rememberHighlighting(find = find, paragraphs = room.paragraphs)
    var hasFocus by remember { mutableStateOf(value = false) }
    var layout by remember { mutableStateOf<() -> TextLayoutResult?>(value = { null }) }
    var top by remember { mutableFloatStateOf(value = 0f) }
    val label = stringResource(resource = Res.string.editor_body_label)
    val onKey = rememberEditorKeys(buffer = buffer, platformConventions = platformConventions, onIntent = onIntent)
    ScrollToCurrentMatch(scrollState = scrollState, layout = { layout() }, top = { top }, find = find)
    BasicTextField(
        state = buffer,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { top = it.positionInParent().y }
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
                onIntent = {},
                properties = EditorPreviewData.properties
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
