package app.notekea.notes.presentation.templates

import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.TemplateSettings

/**
 * State of the template picker (`ROADMAP.md` 1.12): the templates matching [query] as [rows], and [selectedIndex] the
 * one `Enter` chooses, for [target]. [hasFolder] is false while the vault has no templates folder ([folder]), which the
 * picker offers to create ([isCreating] meanwhile, [error] when it failed); [hasTemplates] tells an empty folder from a
 * search that matches nothing.
 */
data class TemplatePickerState(
    val isOpen: Boolean = false,
    val target: TemplateTarget = TemplateTarget.CurrentNote,
    val query: String = "",
    val rows: List<TemplatePickerRow> = emptyList(),
    val selectedIndex: Int = 0,
    val folder: String = TemplateSettings.DEFAULT_FOLDER,
    val hasFolder: Boolean = true,
    val hasTemplates: Boolean = false,
    val isCreating: Boolean = false,
    val error: NotesError? = null
)
