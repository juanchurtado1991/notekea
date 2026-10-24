package app.notekea.notes.presentation.tree

/** Something the user should know after an action that worked, shown until dismissed. */
sealed interface TreeNotice {
    /** The system trash was not available, so the entry went to the vault's `.trash` folder instead. */
    data class MovedToVaultTrash(val name: String) : TreeNotice

    /** Links were updated after a rename; [skippedCount] notes changed meanwhile and were left as they were. */
    data class LinksUpdated(
        val updatedCount: Int,
        val skippedCount: Int
    ) : TreeNotice
}
