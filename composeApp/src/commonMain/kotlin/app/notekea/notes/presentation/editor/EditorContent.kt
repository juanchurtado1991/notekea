package app.notekea.notes.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaFindBar
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMarkdown
import app.notekea.core.presentation.components.NotekeaSkeletonRow
import app.notekea.core.presentation.components.NotekeaTitleField
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.core.presentation.theme.notekeaCodeTextStyle
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.NotesError
import app.notekea.notes.presentation.tree.notesErrorMessage
import app.notekea.resources.Res
import app.notekea.resources.editor_body_label
import app.notekea.resources.editor_edit_mode
import app.notekea.resources.editor_find
import app.notekea.resources.editor_find_count
import app.notekea.resources.editor_find_none
import app.notekea.resources.editor_read_mode
import app.notekea.resources.editor_saved
import app.notekea.resources.editor_title_label
import app.notekea.resources.editor_unsaved
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

private const val BODY_MIN_HEIGHT_DP = 320
private const val CONTENT_MAX_WIDTH_DP = 760
private const val MATCH_SCROLL_MARGIN_DP = 96
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
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column {
            EditorBanners(state = state, platformConventions = platformConventions, onIntent = onIntent)
            state.find?.let { find -> EditorFindBar(find = find, onIntent = onIntent) }
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(state = scrollState),
                contentAlignment = Alignment.TopCenter
            ) {
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
                    if (state.isLoading) {
                        EditorSkeleton()
                    } else if (state.isReading) {
                        ReadView(text = buffer.text.toString(), onIntent = onIntent)
                    } else {
                        EditorBody(
                            buffer = buffer,
                            scrollState = scrollState,
                            onIntent = onIntent,
                            find = state.find,
                            focusRequester = textFocusRequester
                        )
                    }
                }
            }
        }
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
            text = folder.value.split('/').joinToString(separator = PATH_SEPARATOR),
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
        FindButton(platformConventions = platformConventions, onIntent = onIntent)
        ModeToggle(isReading = state.isReading, platformConventions = platformConventions, onIntent = onIntent)
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
    val tags = properties?.tags.orEmpty().joinToString(separator = " ") { TAG_PREFIX + it }
    val aliases = properties?.aliases.orEmpty().joinToString(separator = PROPERTY_SEPARATOR)
    return listOf(tags, aliases).filter { it.isNotEmpty() }.joinToString(separator = PROPERTY_SEPARATOR).ifEmpty {
        null
    }
}

/** Opens Find in note, so it can be found without knowing its shortcut, which the tooltip teaches. */
@Composable
private fun FindButton(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    NotekeaIconButton(
        icon = NotekeaIcons.Find,
        label = stringResource(resource = Res.string.editor_find),
        onClick = { onIntent(EditorIntent.OnFindOpen) },
        modifier = Modifier.padding(start = NotekeaDimensions.SpacingS),
        shortcutHint = platformConventions.findShortcut.label(operatingSystem = platformConventions.operatingSystem)
    )
}

/** Switches between Edit and Read mode; the icon and tooltip name the mode it switches to, with its shortcut. */
@Composable
private fun ModeToggle(
    isReading: Boolean,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    NotekeaIconButton(
        icon = if (isReading) NotekeaIcons.EditMode else NotekeaIcons.ReadMode,
        label = stringResource(resource = if (isReading) Res.string.editor_edit_mode else Res.string.editor_read_mode),
        onClick = { onIntent(EditorIntent.OnReadModeToggle) },
        modifier = Modifier.padding(start = NotekeaDimensions.SpacingS),
        shortcutHint = platformConventions.toggleReadModeShortcut.label(
            operatingSystem = platformConventions.operatingSystem
        )
    )
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
    find: FindState?
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
            findState = { currentFind.value }
        )
    }
}

@Composable
private fun EditorBody(
    buffer: TextFieldState,
    scrollState: ScrollState,
    onIntent: (EditorIntent) -> Unit,
    find: FindState?,
    focusRequester: FocusRequester?
) {
    val colors = MaterialTheme.colorScheme
    val highlighting = rememberHighlighting(find = find)
    var hasFocus by remember { mutableStateOf(value = false) }
    var layout by remember { mutableStateOf<() -> TextLayoutResult?>(value = { null }) }
    var top by remember { mutableFloatStateOf(value = 0f) }
    val label = stringResource(resource = Res.string.editor_body_label)
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
            .onFocusChanged {
                if (hasFocus && !it.isFocused) onIntent(EditorIntent.OnFocusLost)
                hasFocus = it.isFocused
            },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        cursorBrush = SolidColor(value = colors.primary),
        onTextLayout = { getResult -> layout = getResult },
        outputTransformation = highlighting
    )
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

/** The note rendered: wiki links become links; a click opens the note, or the browser for a web address. */
@Composable
private fun ReadView(
    text: String,
    onIntent: (EditorIntent) -> Unit
) {
    val readLinks = remember { ReadModeLinks() }
    val content = remember(text) { readLinks.prepare(body = text) }
    val systemUriHandler = LocalUriHandler.current
    NotekeaMarkdown(
        content = content,
        modifier = Modifier.padding(top = NotekeaDimensions.SpacingL),
        onLinkClick = { uri ->
            when (val link = readLinks.classify(uri = uri)) {
                is ReadLink.Note -> onIntent(EditorIntent.OnLinkClick(link = link))
                is ReadLink.External -> systemUriHandler.openUri(uri = link.uri)
                is ReadLink.Tag -> onIntent(EditorIntent.OnTagClick(tag = link.name))
                null -> Unit
            }
        }
    )
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
