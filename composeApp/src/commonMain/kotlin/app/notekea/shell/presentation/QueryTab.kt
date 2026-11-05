package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.editor.presentation.EditorIntent
import app.notekea.query.presentation.QueryBlock
import app.notekea.query.presentation.QueryEffect
import app.notekea.query.presentation.QueryIntent
import app.notekea.query.presentation.QueryViewModel
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.VaultTreeState

/**
 * What queries follow while the vault is open: its notes and whether their tab shows, as they run only then. A note
 * chosen in a table opens in a tab with the keyboard in its text, and a note a table wrote reaches the editor that has
 * it open.
 */
@Composable
internal fun FollowQuery(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState
) {
    val query = viewModels.query
    val tabs by viewModels.workspace.state.collectAsState()
    val isShown = tabs.workspace.panes.any { it.activePath == Workspace.QUERY }
    LaunchedEffect(query, state.tree.notes) {
        query.onIntent(intent = QueryIntent.OnNotesChange(notes = state.tree.notes.values))
    }
    LaunchedEffect(query, isShown) { query.onIntent(intent = QueryIntent.OnShownChange(isShown = isShown)) }
    LaunchedEffect(query) {
        query.effects.collect { effect ->
            when (effect) {
                is QueryEffect.OpenNote -> {
                    viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path))
                    val pane = viewModels.workspace.state.value.workspace.paneOpening(path = effect.path)
                    viewModels.editors[pane].onIntent(intent = EditorIntent.OnFocusRequest(path = effect.path))
                }

                is QueryEffect.NoteRewritten -> viewModels.editors.forEach { editor ->
                    editor.onIntent(intent = EditorIntent.OnNoteRewritten(path = effect.path))
                }
            }
        }
    }
}

/** How Read mode draws a query block of this vault. */
internal fun queryBlockOf(
    query: QueryViewModel,
    notes: Collection<NoteEntry>
): @Composable (String, String) -> Unit = { text, source ->
    QueryBlock(
        text = text,
        source = source,
        notes = notes,
        notice = query.state.collectAsState().value.notice,
        run = { query.resultOf(query = it, notes = notes) },
        onIntent = query::onIntent
    )
}
