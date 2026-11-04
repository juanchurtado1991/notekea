package app.notekea.notes.presentation.related

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaPassageRow
import app.notekea.core.presentation.components.NotekeaSectionHeader
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.RelatedNote
import app.notekea.notes.domain.RelatedSnippet
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.connections.ConnectionActions
import app.notekea.notes.presentation.connections.ConnectionsMuted
import app.notekea.notes.presentation.connections.folderOf
import app.notekea.resources.Res
import app.notekea.resources.related_empty
import app.notekea.resources.related_error_index
import app.notekea.resources.related_error_model
import app.notekea.resources.related_insert_link
import app.notekea.resources.related_linked
import app.notekea.resources.related_not_ready
import app.notekea.resources.related_open_beside
import org.jetbrains.compose.resources.stringResource

/**
 * Connections' Related tab (`ROADMAP.md` 2.4, design A): a row per related note (name, folder, the passage that
 * matched) with Open to the side and Insert link on hover, a click opening it; or why there is none; then "Already
 * linked", folded unless opened.
 */
@Composable
fun RelatedList(
    state: RelatedNotesState,
    onIntent: (RelatedNotesIntent) -> Unit,
    actions: ConnectionActions
) {
    Column {
        statusText(state = state)?.let { ConnectionsMuted(text = it) }
        state.related.forEach { RelatedRow(note = it, actions = actions) }
        Linked(state = state, onIntent = onIntent, onOpen = actions.onOpen)
    }
}

/** Why no row shows, if none does: the index not ready yet, an error, or nothing related. */
@Composable
private fun statusText(
    state: RelatedNotesState
): String? = when {
    state.error == IndexRunError.Model -> stringResource(Res.string.related_error_model)
    state.error == IndexRunError.Index -> stringResource(Res.string.related_error_index)
    state.related.isNotEmpty() || state.isLoading -> null
    !state.isIndexReady -> stringResource(Res.string.related_not_ready)
    else -> stringResource(Res.string.related_empty)
}

@Composable
private fun RelatedRow(
    note: RelatedNote,
    actions: ConnectionActions
) {
    val path = note.path
    NotekeaPassageRow(
        title = path.nameWithoutExtension,
        subtitle = folderOf(path = path),
        text = note.snippet.text,
        highlight = note.snippet.highlight,
        icon = NotekeaIcons.Note,
        onClick = { actions.onOpen(path) }
    ) {
        NotekeaIconButton(
            icon = NotekeaIcons.SidePanel,
            label = stringResource(Res.string.related_open_beside),
            onClick = { actions.onOpenBeside(path) }
        )
        NotekeaIconButton(
            icon = NotekeaIcons.NoteLink,
            label = stringResource(Res.string.related_insert_link),
            onClick = { actions.onInsertLink(path) }
        )
    }
}

/** "Already linked", folded unless opened: the notes the open note links, each opening on a click. */
@Composable
private fun Linked(
    state: RelatedNotesState,
    onIntent: (RelatedNotesIntent) -> Unit,
    onOpen: (RelativePath) -> Unit
) {
    if (state.linked.isEmpty()) return
    NotekeaSectionHeader(
        title = stringResource(Res.string.related_linked),
        isOpen = state.isLinkedOpen,
        onToggle = { onIntent(RelatedNotesIntent.OnLinkedToggle) },
        count = state.linked.size
    )
    if (!state.isLinkedOpen) return
    state.linked.forEach { path ->
        Text(
            text = path.nameWithoutExtension,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { onOpen(path) }
                .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun RelatedListPreview() {
    val snippet = RelatedSnippet(text = PREVIEW_TEXT, highlight = 0..PREVIEW_TEXT.lastIndex)
    NotekeaPreviewThemes {
        RelatedList(
            state = RelatedNotesState(
                related = listOf(RelatedNote(path = RelativePath(value = PREVIEW_NOTE), score = 1f, snippet = snippet)),
                linked = listOf(RelativePath(value = PREVIEW_LINKED)),
                isIndexReady = true
            ),
            onIntent = {},
            actions = ConnectionActions(onOpen = {}, onReveal = { _, _ -> }, onOpenBeside = {}, onInsertLink = {})
        )
    }
}

private const val PREVIEW_NOTE = "Recipes/Tomato soup.md"
private const val PREVIEW_LINKED = "Recipes/Weekly menu.md"
private const val PREVIEW_TEXT = "Summer tomatoes from the beds make it sweeter."
