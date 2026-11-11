package app.notekea.shell.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.domain.StringPatterns
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaStatusBar
import app.notekea.core.presentation.designsystem.components.NotekeaStatusLabel
import app.notekea.core.presentation.designsystem.components.NotekeaStatusProgress
import app.notekea.core.presentation.designsystem.components.NotekeaStatusSeparator
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaColors
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.platform.LocalUiZoom
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.SaveStatus
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
import app.notekea.resources.status_reveal
import app.notekea.resources.status_words
import app.notekea.resources.status_zoom
import app.notekea.resources.status_zoom_tooltip
import app.notekea.resources.tab_graph
import app.notekea.resources.tab_query
import app.notekea.resources.tab_study
import app.notekea.resources.tab_tasks
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val PERCENT = 100
private const val PREVIEW_PATH = "Editor/Tasks and callouts.md"
private const val PREVIEW_VAULT = "Playground Vault"

/**
 * The workspace's status bar: where the user is ([path] of [kind]) on the left, a click showing it in Files
 * ([onReveal]); on the right the vault's first scan ([isLoading]), the search index ([searchIndex]), then, for a note
 * ([editor], [buffer]) the cursor's place, line endings, words and saved state.
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
    buffer: TextFieldState? = null,
    searchIndex: @Composable () -> Unit = {}
) {
    NotekeaStatusBar(
        start = {
            NotekeaStatusLabel(
                text = vaultName,
                icon = NotekeaIcons.SwitchVault
            )

            path?.let {
                Breadcrumbs(
                    path = it,
                    kind = kind,
                    onReveal = onReveal
                )
            }
        },
        end = {
            if (isLoading) {
                NotekeaStatusProgress(
                    label = stringResource(resource = Res.string.status_loading)
                )
            }

            searchIndex()

            editor?.let {
                NoteFacts(
                    editor = it,
                    buffer = buffer
                )
            }

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

    generateSequence(seed = path.parent) { it.parent }
        .filterNot { it.isRoot }
        .toList()
        .asReversed()
        .forEach {
            Chevron()

            NotekeaStatusLabel(
                text = it.name,
                tooltip = reveal,
                onClick = { onReveal(it) }
            )
        }

    Chevron()

    val icon = when (kind) {
        TabKind.NOTE -> NotekeaIcons.Note
        TabKind.FOLDER -> NotekeaIcons.Folder
        TabKind.GRAPH -> NotekeaIcons.Graph
        TabKind.QUERY -> NotekeaIcons.Query
        TabKind.TASKS -> NotekeaIcons.Tasks
        TabKind.STUDY -> NotekeaIcons.Study
        else -> NotekeaIcons.OtherFile
    }

    val text = when (kind) {
        TabKind.NOTE -> path.nameWithoutExtension
        TabKind.GRAPH -> stringResource(resource = Res.string.tab_graph)
        TabKind.QUERY -> stringResource(resource = Res.string.tab_query)
        TabKind.TASKS -> stringResource(resource = Res.string.tab_tasks)
        TabKind.STUDY -> stringResource(resource = Res.string.tab_study)
        else -> path.name
    }

    NotekeaStatusLabel(
        tooltip = reveal,
        onClick = { onReveal(path) },
        text = text,
        icon = icon
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

/** The cursor's line and column, the line endings, the encoding, the words and the save state. */
@Composable
private fun NoteFacts(
    editor: EditorState,
    buffer: TextFieldState?
) {
    if (buffer != null) {
        val (line, column) = cursorOf(
            text = buffer.text,
            offset = buffer.selection.start
        )

        val fileLine = line + editor.bodyStartLine

        NotekeaStatusLabel(
            text = stringResource(Res.string.status_cursor, fileLine, column),
            tooltip = stringResource(Res.string.status_cursor_tooltip, fileLine, column)
        )

        NotekeaStatusSeparator()
    }

    NotekeaStatusLabel(
        text = stringResource(
            resource = if (editor.usesCrLf) Res.string.status_crlf else Res.string.status_lf
        ),
        tooltip = stringResource(resource = Res.string.status_line_endings_tooltip)
    )

    NotekeaStatusSeparator()

    NotekeaStatusLabel(
        text = stringResource(resource = Res.string.status_encoding),
        tooltip = stringResource(resource = Res.string.status_encoding_tooltip)
    )

    editor
        .info
        .stats
        ?.let { stats ->
            NotekeaStatusSeparator()

            NotekeaStatusLabel(
                text = pluralStringResource(Res.plurals.status_words, stats.words, stats.words)
            )
        }

    NotekeaStatusSeparator()
    val isUnsaved = editor.saveStatus == SaveStatus.UNSAVED

    NotekeaStatusLabel(
        text = stringResource(
            resource = if (isUnsaved) Res.string.editor_unsaved else Res.string.editor_saved
        ),
        icon = if (isUnsaved) NotekeaIcons.InfoModified else NotekeaIcons.Checked,
        iconTint = if (isUnsaved) NotekeaColors.warning else NotekeaColors.success
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
    // Counted in place: it runs on every keystroke and caret move, so the text before the cursor is never copied.
    val end = offset.coerceIn(
        minimumValue = 0,
        maximumValue = text.length
    )

    var lines = 1
    var lineStart = 0

    for (index in 0 until end) {
        if (text[index] == StringPatterns.NEW_LINE_CHAR) {
            lines++
            lineStart = index + 1
        }
    }

    return lines to end - lineStart + 1
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
