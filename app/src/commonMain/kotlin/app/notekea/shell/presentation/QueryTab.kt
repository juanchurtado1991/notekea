package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.editor.presentation.view.LocalQueryBlock
import app.notekea.query.presentation.QueryBlock
import app.notekea.query.presentation.QueryEffect
import app.notekea.query.presentation.QueryIntent
import app.notekea.query.presentation.QueryNotice
import app.notekea.query.presentation.QueryViewModel
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.model.VaultTreeState

/** What the vault's tools follow: the graph, queries and tasks. */
@Composable
internal fun FollowTools(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState,
    searchIndex: SearchIndexViewModel,
    sidePanel: SidePanelHost
) {
    FollowQuery(
        viewModels = viewModels,
        state = state
    )

    FollowTasks(
        viewModels = viewModels,
        state = state
    )

    FollowStudy(
        viewModels = viewModels,
        state = state
    )

    FollowGraph(
        viewModels = viewModels,
        state = state,
        searchIndex = searchIndex,
        sidePanel = sidePanel
    )
}

/**
 * What queries follow while the vault is open: its notes and whether their tab shows, as they run only then. A note
 * chosen in a table opens in a tab with the keyboard in its text, and a note a table wrote reaches the editor that has
 * it open.
 */
@Composable
private fun FollowQuery(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState
) {
    val query = viewModels.query

    val tabs by viewModels
        .workspace
        .state
        .collectAsState()

    val isShown = tabs
        .workspace
        .panes
        .any {
            it.activePath == Workspace.QUERY
        }

    LaunchedEffect(query, state.tree.notes) {
        query.onIntent(intent = QueryIntent.OnNotesChange(notes = state.tree.notes.values))
    }

    LaunchedEffect(query, isShown) {
        query.onIntent(intent = QueryIntent.OnShownChange(isShown = isShown))
    }

    LaunchedEffect(query) {
        query.effects.collect { effect ->
            when (effect) {
                is QueryEffect.OpenNote -> {
                    viewModels.workspace.onIntent(
                        intent = WorkspaceIntent.OnOpen(path = effect.path)
                    )

                    val pane = viewModels
                        .workspace
                        .state
                        .value
                        .workspace
                        .paneOpening(path = effect.path)

                    viewModels.editors[pane].onIntent(
                        intent = EditorIntent.OnFocusRequest(path = effect.path)
                    )
                }

                is QueryEffect.NoteRewritten -> viewModels.editors.forEach { editor ->
                    editor.onIntent(intent = EditorIntent.OnNoteRewritten(path = effect.path))
                }
            }
        }
    }
}

/** What the notes' query blocks follow, for [content]: each runs against the notes [query] knows. */
@Composable
internal fun FollowQueryBlocks(
    query: QueryViewModel,
    content: @Composable () -> Unit
) {
    val block: @Composable (String, String) -> Unit = remember(query) {
        { text, source ->
            val queryState by query.state.collectAsState()

            QueryBlock(
                text = text,
                source = source,
                notesVersion = queryState.notesVersion,
                run = query::resultOf,
                onIntent = query::onIntent,
                notice = queryState.notice.takeIf {
                    it is QueryNotice.WriteFailed
                }
            )
        }
    }

    CompositionLocalProvider(LocalQueryBlock provides block, content = content)
}
