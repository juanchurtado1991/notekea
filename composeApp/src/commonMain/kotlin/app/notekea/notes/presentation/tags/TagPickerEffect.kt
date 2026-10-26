package app.notekea.notes.presentation.tags

/** One-shot events of the tag picker. */
sealed interface TagPickerEffect {
    /** A tag was chosen, from the vault or new: the editor writes it at the cursor. */
    data class TagChosen(val name: String) : TagPickerEffect

    /** Closed without choosing: the note's text gets the keyboard and its selection back. */
    data object Dismissed : TagPickerEffect
}
