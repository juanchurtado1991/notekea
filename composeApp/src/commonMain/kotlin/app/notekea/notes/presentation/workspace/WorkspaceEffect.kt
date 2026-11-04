package app.notekea.notes.presentation.workspace

/** One-shot events of the tabs for the screen. */
sealed interface WorkspaceEffect {
    /** The outline of the active pane should take the keyboard (`Mod+Shift+O`). */
    data object FocusOutline : WorkspaceEffect

    /** Something unexpected went wrong with the tabs (restoring or keeping them): the window says so. */
    data object UnexpectedError : WorkspaceEffect
}
