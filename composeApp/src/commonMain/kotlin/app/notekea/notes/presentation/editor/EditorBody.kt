package app.notekea.notes.presentation.editor

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.matches
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.LocalNotekeaCodeTextStyle
import app.notekea.notes.domain.ListAction
import app.notekea.notes.domain.ListEditing
import app.notekea.resources.Res
import app.notekea.resources.editor_body_label
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private const val BODY_MIN_HEIGHT_DP = 320
private const val MATCH_SCROLL_MARGIN_DP = 96

/** Markdown highlighting plus the find matches, which are read inside the transformation so they restyle at once. */
@Composable
private fun rememberHighlighting(
    find: FindState?,
    paragraphs: (String) -> List<Pair<IntRange, ParagraphStyle>>
): MarkdownOutputTransformation {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val codeFontFamily = LocalNotekeaCodeTextStyle.current.fontFamily
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

/** The note's text in Edit mode: its Markdown highlighted, find matches, tasks' checkboxes and the files it embeds. */
@Composable
internal fun EditorBody(
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

@Preview
@Composable
private fun EditorBodyPreview() {
    NotekeaPreviewThemes {
        EditorBody(
            buffer = EditorPreviewData.buffer(),
            scrollState = rememberScrollState(),
            platformConventions = EditorPreviewData.platformConventions,
            onIntent = {},
            attachments = emptyList(),
            outline = OutlinePlacement(headings = EditorPreviewData.outline, positions = HeadingPositions { null }),
            find = null,
            focusRequester = null
        )
    }
}
