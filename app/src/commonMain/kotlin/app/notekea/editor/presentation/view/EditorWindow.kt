package app.notekea.editor.presentation.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setSelection
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.textSelectionRange
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.core.domain.StringPatterns.WINDOWS_NEW_LINE
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalNotekeaCodeTextStyle
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.markdown.CellInput
import app.notekea.editor.presentation.markdown.KeepInWindow
import app.notekea.editor.presentation.markdown.MarkdownOutputTransformation
import app.notekea.editor.presentation.markdown.MarkdownStyles
import app.notekea.editor.presentation.markdown.OnlyNewLines
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.reading.ReadingView
import app.notekea.editor.presentation.toolbar.formatActions
import app.notekea.resources.Res
import app.notekea.resources.editor_body_label
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

/** Markdown highlighting plus the find matches, which are read inside the transformation so they restyle at once. */
@Composable
private fun rememberHighlighting(
    page: EditorPageState,
    find: FindState?
): MarkdownOutputTransformation {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val codeFontFamily = LocalNotekeaCodeTextStyle.current.fontFamily
    val currentFind = rememberUpdatedState(newValue = find)

    return remember(page, colors, typography, codeFontFamily) {
        MarkdownOutputTransformation(
            styles = MarkdownStyles.from(
                colorScheme = colors,
                typography = typography,
                codeFontFamily = codeFontFamily
            ),
            shown = { page.shown },
            findState = { currentFind.value }
        )
    }
}

/**
 * The text field of the editor's list, over the blocks the cursor is in (the window): the note's one text field, which
 * shows only those blocks and takes their place in the list. It stays composed while it is scrolled out of view, so it
 * keeps the keyboard and typing brings it back.
 */
@Composable
internal fun EditorWindow(
    page: EditorPageState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit,
    find: FindState?,
    modifier: Modifier = Modifier
) {
    val label = stringResource(resource = Res.string.editor_body_label)
    val formats = formatActions()

    val keys = remember(page, platformConventions, formats, onIntent) {
        EditorKeys(
            page = page,
            platformConventions = platformConventions,
            formats = formats,
            onIntent = onIntent
        )
    }

    KeepComposed(page = page)
    PlaceClick(page = page)

    BasicTextField(
        state = page.buffer,
        inputTransformation = remember(page) {
            OnlyNewLines
                .then(next = KeepInWindow { page.fieldRange })
                .then(
                    next = CellInput {
                        page.cell != null
                    }
                )
        },
        modifier = modifier
            .fillMaxWidth()
            .onPlaced { page.fieldPlace = it }
            .wholeNote(
                buffer = page.buffer,
                label = label
            )
            .focusRequester(focusRequester = page.focusRequester)
            .onPreviewKeyEvent(onPreviewKeyEvent = keys::onKey)
            .onFocusChanged {
                if (page.hasFocus && !it.isFocused) onIntent(EditorIntent.OnFocusLost)
                page.hasFocus = it.isFocused
            },
        // Ligatures would turn what is typed into other glyphs (`->` into an arrow): the editor shows the characters.
        textStyle = MaterialTheme
            .typography
            .bodyLarge
            .copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFeatureSettings = NO_LIGATURES
            ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        onTextLayout = { getResult -> page.layout = getResult },
        outputTransformation = rememberHighlighting(
            page = page,
            find = find
        ),
        decorator = remember(page, onIntent) {
            decoratorOf(
                layout = page::currentLayout,
                shown = { page.shown },
                onIntent = onIntent
            )
        }
    )
}

/**
 * The embedded files and the query blocks of the window's text, drawn under the field, which shows them as written:
 * the cursor's block keeps its pictures and its query's table. They are read here, so typing recomposes only this.
 */
@Composable
internal fun WindowEmbeds(
    page: EditorPageState,
    attachments: List<AttachmentView>,
    onIntent: (EditorIntent) -> Unit
) {
    val shown = page.items.shown

    ReadingView(
        text = page
            .buffer
            .text
            .substring(
                startIndex = shown.min,
                endIndex = shown.max
            ),
        attachments = attachments,
        onIntent = onIntent,
        modifier = Modifier.padding(top = NotekeaDimensions.SpacingM),
        isSelectable = false,
        isTextShown = false,
        textStart = shown.min
    )
}

/**
 * What the field tells a screen reader and the Auto-Test: the note's whole text and the selection's place in it, not
 * only the window the field shows, whose text and offsets change as the cursor moves between blocks. A text set
 * through it replaces the whole note.
 */
private fun Modifier.wholeNote(
    buffer: TextFieldState,
    label: String
): Modifier = semantics {
    contentDescription = label
    editableText = AnnotatedString(text = buffer.text.toString())
    textSelectionRange = buffer.selection

    setSelection { start, end, _ ->
        val length = buffer.text.length

        buffer.edit {
            selection = TextRange(
                start = start.coerceIn(
                    minimumValue = 0,
                    maximumValue = length
                ),
                end = end.coerceIn(
                    minimumValue = 0,
                    maximumValue = length
                )
            )
        }

        true
    }

    setText { text ->
        buffer.edit {
            replace(
                start = 0,
                end = length,
                text = text.text.replace(
                    oldValue = WINDOWS_NEW_LINE,
                    newValue = NEW_LINE
                )
            )
        }

        true
    }
}

/** Keeps the field's item composed while the list scrolls it out of view, and lets go of its layout when it leaves. */
@Composable
private fun KeepComposed(page: EditorPageState) {
    val pinnable = LocalPinnableContainer.current

    DisposableEffect(page, pinnable) {
        val pin = pinnable?.pin()

        onDispose {
            pin?.release()
            page.layout = { null }
            page.fieldPlace = null
        }
    }
}

/**
 * A click on a drawn block made it the field's: once the field has laid it out, the cursor goes where the click was.
 */
@Composable
private fun PlaceClick(page: EditorPageState) {
    val waiting = page.placement

    LaunchedEffect(page, waiting) {
        if (waiting == null) return@LaunchedEffect

        val layout = snapshotFlow { page.currentLayout() }.filterNotNull().first()

        page.place(
            waiting = waiting,
            layout = layout
        )

        page.focusRequester.requestFocus()
    }
}

@Preview
@Composable
private fun EditorWindowPreview() {
    val page = rememberPreviewPage()

    NotekeaPreviewThemes {
        EditorWindow(
            page = page,
            find = null,
            platformConventions = EditorPreviewData.platformConventions,
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun WindowEmbedsPreview() {
    val page = rememberPreviewPage()

    NotekeaPreviewThemes {
        WindowEmbeds(
            page = page,
            attachments = emptyList(),
            onIntent = {}
        )
    }
}

@Composable
private fun rememberPreviewPage(): EditorPageState {
    val buffer = remember { EditorPreviewData.buffer() }
    val listState = rememberLazyListState()

    return remember(buffer, listState) {
        EditorPageState(
            buffer = buffer,
            listState = listState,
            focusRequester = FocusRequester(),
            isSource = { false }
        )
    }
}
