package app.notekea.notes.presentation.editor

/** One-shot events of the note editor for the window around it. */
sealed interface EditorEffect {
    /** The user closed a note that was deleted outside Notekea, so nothing is selected anymore. */
    data object Closed : EditorEffect

    /** Everything pending was saved (or could not be), so the window may close. */
    data object Flushed : EditorEffect
}
