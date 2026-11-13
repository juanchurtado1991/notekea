package app.notekea.shell.presentation.workspace

import app.notekea.cards.presentation.StudyViewModel
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.graph.presentation.viewmodel.GraphViewModel
import app.notekea.query.presentation.QueryViewModel
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.shell.presentation.tabs.DocumentFocus
import app.notekea.tasks.presentation.TasksViewModel
import app.notekea.templates.presentation.TemplatePickerIntent
import app.notekea.tree.presentation.tags.TagPickerIntent
import app.notekea.tree.presentation.viewmodel.VaultTreeViewModel

/**
 * The view models the vault workspace is drawn from, given together: the [graph], the [tree], the [workspace] of tabs,
 * one editor per pane ([editors]), and how to ask the quick switcher, the tag picker and the template picker to open
 * ([onSwitcher], [onTagPicker], [onTemplatePicker]), and the passage of a document to show ([documentFocus]).
 */
internal class WorkspaceViewModels(
    val graph: GraphViewModel,
    val query: QueryViewModel,
    val tasks: TasksViewModel,
    val study: StudyViewModel,
    val tree: VaultTreeViewModel,
    val workspace: WorkspaceViewModel,
    val editors: List<EditorViewModel>,
    val onSwitcher: (QuickSwitcherIntent) -> Unit,
    val onTagPicker: (TagPickerIntent) -> Unit,
    val onTemplatePicker: (TemplatePickerIntent) -> Unit,
    val documentFocus: DocumentFocus = DocumentFocus(),
    val onClipRequest: () -> Unit = {}
)
