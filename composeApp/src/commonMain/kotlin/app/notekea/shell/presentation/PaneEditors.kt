package app.notekea.shell.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.EditorViewModel
import app.notekea.shell.presentation.WorkspaceViewModel

/**
 * One editor per pane of the [workspace], read from its current tabs, so a request sent right after opening a note
 * reaches the pane that shows it, not the one that was active a moment ago.
 */
internal class PaneEditors(
    private val workspace: WorkspaceViewModel,
    private val editors: List<EditorViewModel>
) {
    val active: EditorViewModel
        get() = editors[workspace.state.value.workspace.activePane]

    /** The editor of the pane that shows, or is about to show, the note at [path]. */
    fun of(
        path: RelativePath
    ): EditorViewModel = editors[workspace.state.value.workspace.paneOpening(path = path)]
}
