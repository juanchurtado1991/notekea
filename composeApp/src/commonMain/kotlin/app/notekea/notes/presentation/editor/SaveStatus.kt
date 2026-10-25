package app.notekea.notes.presentation.editor

/** What the editor header says about saving: nothing, "Unsaved" while an autosave is pending, or "Saved". */
enum class SaveStatus {
    NONE,
    SAVED,
    UNSAVED
}
