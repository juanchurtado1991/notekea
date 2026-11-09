package app.notekea.editor.presentation.model

/** What the editor header says about saving: nothing, "Unsaved" while an autosave is pending, or "Saved". */
enum class SaveStatus {
    NONE,
    SAVED,
    UNSAVED
}
