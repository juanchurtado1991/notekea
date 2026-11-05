package app.notekea.connections.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaLazyScrollArea
import app.notekea.core.presentation.components.NotekeaPassageRow
import app.notekea.core.presentation.components.NotekeaSectionHeader
import app.notekea.core.presentation.components.focusBorder
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.core.notes.domain.RelatedNote
import app.notekea.core.notes.domain.PassageSnippet
import app.notekea.core.notes.domain.RelativePath
import app.notekea.connections.presentation.ConnectionActions
import app.notekea.connections.presentation.ConnectionMenu
import app.notekea.connections.presentation.ConnectionsMuted
import app.notekea.connections.presentation.ConnectionsSkeleton
import app.notekea.connections.presentation.connectionAction
import app.notekea.connections.presentation.folderOf
import app.notekea.resources.Res
import app.notekea.resources.related_empty
import app.notekea.resources.related_error_index
import app.notekea.resources.related_error_model
import app.notekea.resources.related_insert_link
import app.notekea.resources.related_linked
import app.notekea.resources.related_not_ready
import app.notekea.resources.related_too_long
import app.notekea.resources.related_open_beside
import org.jetbrains.compose.resources.stringResource

/**
 * Connections' Related tab (`ROADMAP.md` 2.4, design A): a row per related note (name, folder, the passage that
 * matched) with Open to the side and Insert link on hover, a click opening it; or why there is none, bars standing in
 * for the rows the first time they are found; then "Already linked", folded unless opened.
 */
@Composable
fun RelatedList(
    state: RelatedNotesState,
    onIntent: (RelatedNotesIntent) -> Unit,
    actions: ConnectionActions
) {
    val listState = rememberLazyListState()
    NotekeaLazyScrollArea(state = listState) {
        LazyColumn(state = listState) {
            item(key = STATUS_ITEM, contentType = STATUS_ITEM) {
                Column {
                    statusText(state = state)?.let { ConnectionsMuted(text = it) }
                    if (state.related.isEmpty() && state.isLoading && state.error == null) ConnectionsSkeleton()
                }
            }
            items(items = state.related, key = { it.path.value }, contentType = { RELATED_ITEM }) {
                RelatedRow(note = it, actions = actions)
            }
            linkedItems(state = state, onIntent = onIntent, actions = actions)
        }
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
    state.isTooLong -> stringResource(Res.string.related_too_long)
    !state.isIndexReady -> stringResource(Res.string.related_not_ready)
    else -> stringResource(Res.string.related_empty)
}

@Composable
private fun RelatedRow(
    note: RelatedNote,
    actions: ConnectionActions
) {
    val path = note.path
    val beside = stringResource(Res.string.related_open_beside)
    val insertLink = stringResource(Res.string.related_insert_link)
    ConnectionMenu(onOpen = { actions.onOpen(path) }, onOpenBeside = { actions.onOpenBeside(path) }) {
        NotekeaPassageRow(
            title = path.nameWithoutExtension,
            subtitle = folderOf(path = path),
            text = note.snippet.text,
            highlights = note.snippet.keywords,
            icon = NotekeaIcons.Note,
            onClick = { actions.onOpen(path) },
            accessibilityActions = listOf(
                connectionAction(label = beside) { actions.onOpenBeside(path) },
                connectionAction(label = insertLink) { actions.onInsertLink(path) }
            )
        ) {
            NotekeaIconButton(
                icon = NotekeaIcons.SidePanel,
                label = beside,
                onClick = { actions.onOpenBeside(path) }
            )
            NotekeaIconButton(
                icon = NotekeaIcons.NoteLink,
                label = insertLink,
                onClick = { actions.onInsertLink(path) }
            )
        }
    }
}

/** "Already linked", folded unless opened: the notes the open note links, each opening on a click. */
private fun LazyListScope.linkedItems(
    state: RelatedNotesState,
    onIntent: (RelatedNotesIntent) -> Unit,
    actions: ConnectionActions
) {
    if (state.linked.isEmpty()) return
    item(key = LINKED_HEADER_ITEM, contentType = LINKED_HEADER_ITEM) {
        NotekeaSectionHeader(
            title = stringResource(Res.string.related_linked),
            isOpen = state.isLinkedOpen,
            onToggle = { onIntent(RelatedNotesIntent.OnLinkedToggle) },
            count = state.linked.size
        )
    }
    if (!state.isLinkedOpen) return
    items(items = state.linked, key = { LINKED_KEY_PREFIX + it.value }, contentType = { LINKED_ITEM }) { path ->
        ConnectionMenu(onOpen = { actions.onOpen(path) }, onOpenBeside = { actions.onOpenBeside(path) }) {
            Text(
                text = path.nameWithoutExtension,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusBorder(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    .clickable(role = Role.Button) { actions.onOpen(path) }
                    .padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingXs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
private fun RelatedListPreview() {
    val snippet = PassageSnippet(text = PREVIEW_TEXT, highlight = 0..PREVIEW_TEXT.lastIndex)
    NotekeaPreviewThemes {
        Box(modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)) {
            RelatedList(
                state = RelatedNotesState(
                    related = listOf(
                        RelatedNote(path = RelativePath(value = PREVIEW_NOTE), score = 1f, snippet = snippet)
                    ),
                    linked = listOf(RelativePath(value = PREVIEW_LINKED)),
                    isIndexReady = true
                ),
                onIntent = {},
                actions = ConnectionActions(onOpen = {}, onReveal = { _, _ -> }, onOpenBeside = {}, onInsertLink = {})
            )
        }
    }
}

private const val LINKED_HEADER_ITEM = "linked-header"
private const val LINKED_ITEM = "linked"
private const val LINKED_KEY_PREFIX = "linked:"
private const val PREVIEW_HEIGHT_DP = 200
private const val RELATED_ITEM = "related"
private const val STATUS_ITEM = "status"
private const val PREVIEW_NOTE = "Recipes/Tomato soup.md"
private const val PREVIEW_LINKED = "Recipes/Weekly menu.md"
private const val PREVIEW_TEXT = "Summer tomatoes from the beds make it sweeter."
