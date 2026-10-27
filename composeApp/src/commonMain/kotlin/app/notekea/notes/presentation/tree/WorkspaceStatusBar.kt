package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.StringPatterns
import app.notekea.core.presentation.components.NotekeaStatusBar
import app.notekea.core.presentation.components.NotekeaStatusLabel
import app.notekea.core.presentation.components.NotekeaStatusProgress
import app.notekea.core.presentation.components.NotekeaStatusSeparator
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorState
import app.notekea.notes.presentation.editor.SaveStatus
import app.notekea.notes.presentation.workspace.TabKind
import app.notekea.resources.Res
import app.notekea.resources.editor_saved
import app.notekea.resources.editor_unsaved
import app.notekea.resources.status_crlf
import app.notekea.resources.status_cursor
import app.notekea.resources.status_cursor_tooltip
import app.notekea.resources.status_encoding
import app.notekea.resources.status_encoding_tooltip
import app.notekea.resources.status_lf
import app.notekea.resources.status_line_endings_tooltip
import app.notekea.resources.status_loading
import app.notekea.resources.status_words
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import app.notekea.resources.status_zoom_tooltip
import app.notekea.resources.status_zoom
import app.notekea.resources.status_reveal
import app.notekea.core.presentation.platform.LocalUiZoom

private const val PERCENT = 100
private const val PREVIEW_PATH = "Editor/Tasks and callouts.md"
private const val PREVIEW_VAULT = "Playground Vault"

/**
 * The workspace's status bar (`ROADMAP.md` 1.14), as Android Studio's: where the user is (the vault, the folders and
 * the open tab's file, [path] of [kind]) on the left; on the right the vault's first scan while it runs ([isLoading]),
 * then, for a note in Edit mode ([editor] and its [buffer]), the cursor's line and column in the file, its line endings
 * and encoding, its words, and whether it is saved; last, the window's zoom while it is not 100 %, a click going back to
 * 100 %. A click on a folder or the file of the path shows it in Files ([onReveal]).
 */
@Composable
internal fun WorkspaceStatusBar(
    vaultName: String,
    isLoading: Boolean,
    onReveal: (RelativePath) -> Unit,
    modifier: Modifier = Modifier,
    path: RelativePath? = null,
    kind: TabKind? = null,
    editor: EditorState? = null,
    buffer: TextFieldState? = null
) {
    NotekeaStatusBar(
        start = {
            NotekeaStatusLabel(text = vaultName, icon = NotekeaIcons.SwitchVault)
            path?.let { Breadcrumbs(path = it, kind = kind, onReveal = onReveal) }
        },
        end = {
            if (isLoading) NotekeaStatusProgress(label = stringResource(resource = Res.string.status_loading))
            editor?.let { NoteFacts(editor = it, buffer = buffer) }
            ZoomFact()
        },
        modifier = modifier
    )
}

/** Each folder of [path], then its file, with the icon of its [kind]; a click on one shows it in Files. */
@Composable
private fun Breadcrumbs(
    path: RelativePath,
    kind: TabKind?,
    onReveal: (RelativePath) -> Unit
) {
    val reveal = stringResource(resource = Res.string.status_reveal)
    generateSequence(seed = path.parent) { it.parent }.filterNot { it.isRoot }.toList().asReversed().forEach {
        Chevron()
        NotekeaStatusLabel(text = it.name, tooltip = reveal, onClick = { onReveal(it) })
    }
    Chevron()
    NotekeaStatusLabel(
        tooltip = reveal,
        onClick = { onReveal(path) },
        text = if (kind == TabKind.NOTE) path.nameWithoutExtension else path.name,
        icon = when (kind) {
            TabKind.NOTE -> NotekeaIcons.Note
            TabKind.FOLDER -> NotekeaIcons.Folder
            else -> NotekeaIcons.OtherFile
        }
    )
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = NotekeaIcons.Expand,
        contentDescription = null,
        modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** The cursor's line and column (in Edit mode), the line endings, the encoding, the words and the save state. */
@Composable
private fun NoteFacts(
    editor: EditorState,
    buffer: TextFieldState?
) {
    if (!editor.isReading && buffer != null) {
        val (line, column) = cursorOf(text = buffer.text, offset = buffer.selection.start)
        val fileLine = line + editor.bodyStartLine
        NotekeaStatusLabel(
            text = stringResource(Res.string.status_cursor, fileLine, column),
            tooltip = stringResource(Res.string.status_cursor_tooltip, fileLine, column)
        )
        NotekeaStatusSeparator()
    }
    NotekeaStatusLabel(
        text = stringResource(resource = if (editor.usesCrLf) Res.string.status_crlf else Res.string.status_lf),
        tooltip = stringResource(resource = Res.string.status_line_endings_tooltip)
    )
    NotekeaStatusSeparator()
    NotekeaStatusLabel(
        text = stringResource(resource = Res.string.status_encoding),
        tooltip = stringResource(resource = Res.string.status_encoding_tooltip)
    )
    editor.info.stats?.let { stats ->
        NotekeaStatusSeparator()
        NotekeaStatusLabel(text = pluralStringResource(Res.plurals.status_words, stats.words, stats.words))
    }
    NotekeaStatusSeparator()
    val isUnsaved = editor.saveStatus == SaveStatus.UNSAVED
    NotekeaStatusLabel(
        text = stringResource(resource = if (isUnsaved) Res.string.editor_unsaved else Res.string.editor_saved),
        icon = if (isUnsaved) NotekeaIcons.InfoModified else NotekeaIcons.Checked
    )
}

/** The window's zoom while it is not 100 %, as browsers show it; a click goes back to 100 %. */
@Composable
private fun ZoomFact() {
    val zoom = LocalUiZoom.current
    if (!zoom.isZoomed) return
    NotekeaStatusSeparator()
    NotekeaStatusLabel(
        text = stringResource(Res.string.status_zoom, (zoom.scale * PERCENT).roundToInt()),
        icon = NotekeaIcons.Zoom,
        tooltip = stringResource(Res.string.status_zoom_tooltip, zoom.resetHint),
        onClick = zoom.onReset
    )
}

/** The 1-based line and column of [offset] in [text]. */
internal fun cursorOf(
    text: CharSequence,
    offset: Int
): Pair<Int, Int> {
    val before = text.subSequence(
        startIndex = 0,
        endIndex = offset.coerceIn(minimumValue = 0, maximumValue = text.length)
    )
    val lineStart = before.lastIndexOf(char = StringPatterns.NEW_LINE_CHAR) + 1
    return before.count { it == StringPatterns.NEW_LINE_CHAR } + 1 to before.length - lineStart + 1
}

@Preview
@Composable
private fun WorkspaceStatusBarPreview() {
    NotekeaPreviewThemes {
        WorkspaceStatusBar(
            vaultName = PREVIEW_VAULT,
            isLoading = true,
            onReveal = {},
            path = RelativePath(value = PREVIEW_PATH),
            kind = TabKind.NOTE,
            editor = EditorState(),
            buffer = TextFieldState()
        )
    }
}
