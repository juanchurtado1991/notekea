package app.notekea.notes.presentation.templates

import app.notekea.notes.domain.RelativePath

/** Where a template chosen in the picker goes (`ROADMAP.md` 1.12). */
sealed interface TemplateTarget {
    /** Into the note of the active pane, at the cursor (the whole note when it has no text yet). */
    data object CurrentNote : TemplateTarget

    /** Into a new note made in [folder] ("New note from template", or `Mod+T` with no note open). */
    data class NewNote(val folder: RelativePath) : TemplateTarget
}
