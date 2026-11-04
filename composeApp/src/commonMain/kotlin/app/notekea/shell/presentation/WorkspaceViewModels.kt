package app.notekea.shell.presentation

import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.switcher.QuickSwitcherIntent
import app.notekea.notes.presentation.tags.TagPickerIntent
import app.notekea.templates.presentation.TemplatePickerIntent
import app.notekea.shell.presentation.WorkspaceViewModel
import app.notekea.notes.presentation.tree.VaultTreeViewModel

/**
 * The view models the vault workspace is drawn from, given together: the [tree], the [workspace] of tabs, one editor
 * per pane ([editors]), and how to ask the quick switcher, the tag picker and the template picker to open ([onSwitcher],
 * [onTagPicker], [onTemplatePicker]).
 */
internal class WorkspaceViewModels(
    val tree: VaultTreeViewModel,
    val workspace: WorkspaceViewModel,
    val editors: List<EditorViewModel>,
    val onSwitcher: (QuickSwitcherIntent) -> Unit,
    val onTagPicker: (TagPickerIntent) -> Unit,
    val onTemplatePicker: (TemplatePickerIntent) -> Unit
)
