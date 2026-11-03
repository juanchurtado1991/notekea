package app.notekea.notes.presentation.related

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.NotekeaFold
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaPassageCard
import app.notekea.core.presentation.components.NotekeaScrollArea
import app.notekea.core.presentation.components.NotekeaSectionHeader
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.RelatedNote
import app.notekea.notes.domain.RelatedSnippet
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.related_empty
import app.notekea.resources.related_error_index
import app.notekea.resources.related_error_model
import app.notekea.resources.related_insert_link
import app.notekea.resources.related_linked
import app.notekea.resources.related_not_ready
import app.notekea.resources.related_open
import app.notekea.resources.related_open_beside
import app.notekea.resources.related_title
import org.jetbrains.compose.resources.stringResource

/**
 * The inspector's Related section (`ROADMAP.md` 2.4, design A): its header ([isOpen], [onToggle]) with how many, then
 * a card per related note (title, folder, the passage that matched) with Open, Open to the side and Insert link on
 * hover, or why there is none; "Already linked" folded under them. It scrolls when it does not fit.
 */
@Composable
fun ColumnScope.RelatedSection(
    state: RelatedNotesState,
    isOpen: Boolean,
    onToggle: () -> Unit,
    onIntent: (RelatedNotesIntent) -> Unit,
    actions: RelatedActions
) {
    NotekeaSectionHeader(
        title = stringResource(Res.string.related_title),
        isOpen = isOpen,
        onToggle = onToggle,
        count = state.related.size.takeIf { it > 0 }
    )
    NotekeaFold(isOpen = isOpen, modifier = Modifier.weight(weight = 1f, fill = false)) {
        val scroll = rememberScrollState()
        NotekeaScrollArea(state = scroll) {
            Column(
                modifier = Modifier.verticalScroll(state = scroll).padding(horizontal = NotekeaDimensions.SpacingS),
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
            ) {
                statusText(state = state)?.let { Muted(text = it) }
                state.related.forEach { RelatedCard(note = it, actions = actions) }
                Linked(state = state, onIntent = onIntent, onOpen = actions.onOpen)
            }
        }
    }
}

/** Why no card shows, if none does: the index not ready yet, an error, or nothing related. */
@Composable
private fun statusText(
    state: RelatedNotesState
): String? = when {
    state.error == IndexRunError.MODEL -> stringResource(Res.string.related_error_model)
    state.error == IndexRunError.INDEX -> stringResource(Res.string.related_error_index)
    state.related.isNotEmpty() || state.isLoading -> null
    !state.isIndexReady -> stringResource(Res.string.related_not_ready)
    else -> stringResource(Res.string.related_empty)
}

@Composable
private fun RelatedCard(
    note: RelatedNote,
    actions: RelatedActions
) {
    NotekeaPassageCard(
        title = note.path.nameWithoutExtension,
        subtitle = note.path.parent?.takeUnless { it.isRoot }?.value.orEmpty(),
        text = note.snippet.text,
        highlight = note.snippet.highlight,
        icon = NotekeaIcons.Note,
        onClick = { actions.onOpen(note.path) }
    ) {
        val path = note.path
        CardAction(icon = NotekeaIcons.Note, label = stringResource(Res.string.related_open)) { actions.onOpen(path) }
        CardAction(icon = NotekeaIcons.SidePanel, label = stringResource(Res.string.related_open_beside)) {
            actions.onOpenBeside(path)
        }
        CardAction(icon = NotekeaIcons.NoteLink, label = stringResource(Res.string.related_insert_link)) {
            actions.onInsertLink(path)
        }
    }
}

@Composable
private fun CardAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NotekeaIconButton(icon = icon, label = label, onClick = onClick)
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
                .padding(horizontal = NotekeaDimensions.SpacingS, vertical = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Muted(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun RelatedSectionPreview() {
    val snippet = RelatedSnippet(text = PREVIEW_TEXT, highlight = 0..PREVIEW_TEXT.lastIndex)
    NotekeaPreviewThemes {
        Column {
            RelatedSection(
                state = RelatedNotesState(
                    related = listOf(
                        RelatedNote(path = RelativePath(value = PREVIEW_NOTE), score = 1f, snippet = snippet)
                    ),
                    linked = listOf(RelativePath(value = PREVIEW_LINKED)),
                    isIndexReady = true
                ),
                isOpen = true,
                onToggle = {},
                onIntent = {},
                actions = RelatedActions(onOpen = {}, onOpenBeside = {}, onInsertLink = {})
            )
        }
    }
}

private const val PREVIEW_NOTE = "Recipes/Tomato soup.md"
private const val PREVIEW_LINKED = "Recipes/Weekly menu.md"
private const val PREVIEW_TEXT = "Summer tomatoes from the beds make it sweeter."
