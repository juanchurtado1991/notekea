package app.notekea.notes.presentation.tags

/** One choice of the tag picker: a tag of the vault, or the typed text as a new tag ([isNew]). */
data class TagPickerRow(
    val name: String,
    val isNew: Boolean = false
)
