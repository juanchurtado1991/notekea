package app.notekea.tree.presentation.model

/** Something the user should know after an action that worked, shown for a few seconds or until dismissed. */
sealed interface TreeNotice {
    /** The system trash was not available, so the entry went to the vault's `.trash` folder instead. */
    data class MovedToVaultTrash(val name: String) : TreeNotice

    /**
     * What could not be saved of the note named [note] was kept in the note named [copy], or by the editor until the
     * note opens again when even that could not be written (null).
     */
    data class UnsavedKept(
        val note: String,
        val copy: String?
    ) : TreeNotice

    /**
     * Links were updated after a rename in the notes named [notes] (by name, sorted); [skippedCount] notes changed
     * meanwhile and were left as they were.
     */
    data class LinksUpdated(
        val notes: List<String>,
        val skippedCount: Int
    ) : TreeNotice
}
