package app.notekea.shell.presentation.workspace

import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.model.EditorEffect
import app.notekea.editor.presentation.viewmodel.EditorCallbacks
import app.notekea.editor.presentation.viewmodel.saveInOtherEditors
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.search.presentation.switcher.QuickSwitcherMode
import app.notekea.search.presentation.switcher.QuickSwitcherPurpose
import app.notekea.templates.presentation.TemplatePickerIntent
import app.notekea.templates.presentation.TemplateTarget
import app.notekea.tree.presentation.model.BookmarkIntent
import app.notekea.tree.presentation.model.VaultTreeIntent
import app.notekea.tree.presentation.tags.TagPickerIntent

/** What the editor of [pane], on the note at [path], asks of the tree, the tabs, the pickers and the other editor. */
internal fun editorCallbacksOf(
    viewModels: WorkspaceViewModels,
    pane: Int,
    path: RelativePath
): EditorCallbacks {
    val tree = viewModels.tree
    val workspace = viewModels.workspace

    fun onRenamed(effect: EditorEffect.Renamed) {
        tree.onIntent(
            intent = VaultTreeIntent.OnEntryRenamed(
                from = effect.from,
                to = effect.to,
                links = effect.links
            )
        )

        workspace.onIntent(
            intent = WorkspaceIntent.OnRenamed(
                from = effect.from,
                to = effect.to
            )
        )
    }

    return EditorCallbacks(
        onRenamed = { onRenamed(it) },
        onClosed = { workspace.onIntent(intent = WorkspaceIntent.OnDeleted(path = path)) },
        onOpenLink = {
            tree.onIntent(
                intent = VaultTreeIntent.OnFollowLink(
                    source = it.source,
                    target = it.target,
                    kind = it.kind,
                    inBackground = it.inBackground
                )
            )
        },
        onSearchTag = { tag ->
            viewModels.onSwitcher(
                QuickSwitcherIntent.OnOpen(
                    mode = QuickSwitcherMode.CONTENT,
                    query = TAG_MARK + tag
                )
            )
        },
        onRequestNoteLink = {
            viewModels.onSwitcher(
                QuickSwitcherIntent.OnOpen(
                    mode = QuickSwitcherMode.NOTES,
                    purpose = QuickSwitcherPurpose.LINK
                )
            )
        },
        onRequestTag = { query -> viewModels.onTagPicker(TagPickerIntent.OnOpen(query = query)) },
        onBookmarkToggle = { tree.onIntent(intent = BookmarkIntent.OnToggle(path = it)) },
        onRequestTemplate = {
            viewModels.onTemplatePicker(
                TemplatePickerIntent.OnOpen(target = TemplateTarget.CurrentNote)
            )
        },
        onDayStep = { tree.onIntent(intent = VaultTreeIntent.OnDayStep(step = it)) },
        onRevealFolder = { tree.onIntent(intent = VaultTreeIntent.OnRevealInTree(path = it)) },
        onUnsavedKept = {
            tree.onIntent(
                intent = VaultTreeIntent.OnUnsavedKept(
                    note = it.note,
                    copy = it.copy
                )
            )
        },
        onBeforeOpen = {
            saveInOtherEditors(
                path = it,
                editor = viewModels.editors[pane],
                editors = viewModels.editors
            )
        }
    )
}

private const val TAG_MARK = "#"
