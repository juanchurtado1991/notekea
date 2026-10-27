package app.notekea.notes.presentation.templates

import app.notekea.notes.domain.VaultTree

/** What the user did in the template picker, plus the vault tree as it changes. */
sealed interface TemplatePickerIntent {
    /** `Mod+T`, the toolbar's Template button or a folder's "New note from template". */
    data class OnOpen(val target: TemplateTarget) : TemplatePickerIntent

    data object OnClose : TemplatePickerIntent

    /** `Enter`: chooses the selected template. */
    data object OnConfirm : TemplatePickerIntent

    data class OnQueryChange(val query: String) : TemplatePickerIntent

    data class OnRowClick(val index: Int) : TemplatePickerIntent

    data object OnSelectNext : TemplatePickerIntent

    data object OnSelectPrevious : TemplatePickerIntent

    /** "Create" with no templates folder: makes it with the example template [exampleName] holding [exampleText]. */
    data class OnCreateFolderClick(
        val exampleName: String,
        val exampleText: String
    ) : TemplatePickerIntent

    /** The vault's notes and folders, sent whenever the tree changes. */
    data class OnTreeChange(val tree: VaultTree) : TemplatePickerIntent
}
