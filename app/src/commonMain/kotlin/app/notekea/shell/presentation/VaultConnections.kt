package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.notekea.chat.presentation.ChatViewModel
import app.notekea.connections.presentation.ConnectionActions
import app.notekea.connections.presentation.ConnectionsSection
import app.notekea.connections.presentation.FollowNoteMentions
import app.notekea.connections.presentation.FollowRelatedNotes
import app.notekea.connections.presentation.NoteMentionsViewModel
import app.notekea.connections.presentation.RelatedNotesViewModel
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.FormatAction
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.tree.presentation.model.VaultTreeState

/**
 * The side panel's host, with the vault's [chat] and the inspector's Connections: a row opens its note (a mention
 * where it names the open one), opens it beside, or links to it at the active editor's cursor, among the vault's
 * [notes]; the tab shown is the tabs' side panel preference.
 */
internal fun connectionsSidePanel(
    workspace: WorkspaceViewModel,
    related: RelatedNotesViewModel,
    mentions: NoteMentionsViewModel,
    chat: ChatViewModel,
    editors: PaneEditors,
    notes: () -> Set<RelativePath>
): SidePanelHost {
    val actions = connectionActions(
        workspace = workspace,
        editors = editors,
        notes = notes
    )

    return SidePanelHost(
        send = { workspace.onIntent(intent = workspaceIntentOf(intent = it)) },
        connections = { isOpen, onToggle ->
            val workspaceState by workspace.state.collectAsState()
            val relatedState by related.state.collectAsState()
            val mentionsState by mentions.state.collectAsState()

            ConnectionsSection(
                tab = workspaceState.sidePanel.connections,
                onTabSelect = {
                    workspace.onIntent(intent = WorkspaceIntent.OnConnectionsTabSelect(tab = it))
                },
                isOpen = isOpen,
                onToggle = onToggle,
                related = relatedState,
                mentions = mentionsState,
                onRelatedIntent = related::onIntent,
                onMentionsIntent = mentions::onIntent,
                actions = actions
            )
        },
        chat = { inputFocus ->
            VaultChatPanel(
                chat = chat,
                inputFocus = inputFocus
            )
        }
    )
}

/** What Connections' rows do on the tabs and the [editors]. */
internal fun connectionActions(
    workspace: WorkspaceViewModel,
    editors: PaneEditors,
    notes: () -> Set<RelativePath>
): ConnectionActions {
    fun reveal(
        path: RelativePath,
        range: IntRange
    ) {
        workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = path))

        // A document opens in its own tab at the passage, not in a note's editor.
        if (DocumentFormat.of(path = path) != null) {
            return editors.documents.reveal(
                path = path,
                range = range
            )
        }

        editors.of(path = path).onIntent(
            intent = EditorIntent.OnRevealRequest(
                path = path,
                range = range
            )
        )
    }

    fun insertLink(path: RelativePath) {
        val target = LinkResolver(notePaths = notes()).wikiTarget(path = path)

        editors.active.onIntent(
            intent = EditorIntent.OnFormat(action = FormatAction.NoteLink(target = target))
        )
    }

    return ConnectionActions(
        onOpen = { workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = it)) },
        onReveal = { path, range -> reveal(path, range) },
        onOpenBeside = { workspace.onIntent(intent = WorkspaceIntent.OnOpenBeside(path = it)) },
        onInsertLink = { insertLink(it) }
    )
}

/**
 * Connections follow the vault: Related the active [editor]'s note, its edits and the index's
 * runs ([isIndexReady]); Linked and Unlinked the note and the [tree]'s notes, whatever the index does.
 */
@Composable
fun FollowConnections(
    tree: VaultTreeState,
    editor: EditorViewModel,
    isIndexReady: Boolean,
    related: RelatedNotesViewModel,
    mentions: NoteMentionsViewModel
) {
    val editorState by editor.state.collectAsState()

    FollowRelatedNotes(
        buffer = editor.buffer,
        notes = tree.tree.notes.keys,
        isIndexReady = isIndexReady,
        onIntent = related::onIntent,
        note = editorState.notePath
    )

    FollowNoteMentions(
        notes = tree.tree.notes,
        onIntent = mentions::onIntent,
        note = editorState.notePath
    )
}

/** The side panel's requests from the editor's toolbar and outline, as the tabs' intents. */
internal fun workspaceIntentOf(intent: EditorIntent): WorkspaceIntent = when (intent) {
    is EditorIntent.OnSidePanelResize -> WorkspaceIntent.OnSidePanelResize(widthDp = intent.widthDp)

    is EditorIntent.OnSidePanelSectionToggle -> WorkspaceIntent.OnSidePanelSectionToggle(
        section = intent.section
    )

    is EditorIntent.OnSidePanelModeSelect -> WorkspaceIntent.OnSidePanelModeSelect(
        mode = intent.mode
    )

    EditorIntent.OnOutlineFocusRequest -> WorkspaceIntent.OnOutlineFocusRequest

    else -> WorkspaceIntent.OnSidePanelToggle
}
