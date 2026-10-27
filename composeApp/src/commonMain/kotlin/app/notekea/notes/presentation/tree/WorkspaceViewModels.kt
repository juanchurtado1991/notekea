package app.notekea.notes.presentation.tree

import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.switcher.QuickSwitcherIntent
import app.notekea.notes.presentation.tags.TagPickerIntent
import app.notekea.notes.presentation.workspace.WorkspaceViewModel

/**
 * The view models the vault workspace is drawn from, given together: the [tree], the [workspace] of tabs, one editor
 * per pane ([editors]), and how to ask the quick switcher and the tag picker to open ([onSwitcher], [onTagPicker]).
 */
internal class WorkspaceViewModels(
    val tree: VaultTreeViewModel,
    val workspace: WorkspaceViewModel,
    val editors: List<EditorViewModel>,
    val onSwitcher: (QuickSwitcherIntent) -> Unit,
    val onTagPicker: (TagPickerIntent) -> Unit
)
