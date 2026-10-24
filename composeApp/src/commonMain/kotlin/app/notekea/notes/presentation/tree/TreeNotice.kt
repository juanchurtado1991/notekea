package app.notekea.notes.presentation.tree

/** Something the user should know after an action that worked, shown until dismissed. */
sealed interface TreeNotice {
    /** The system trash was not available, so the entry went to the vault's `.trash` folder instead. */
    data class MovedToVaultTrash(val name: String) : TreeNotice
}
