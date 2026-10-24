package app.notekea.notes.domain

/** One change inside a watched vault, with its path relative to the vault. */
sealed interface VaultChange {
    data class Created(
        val path: RelativePath,
        val isFolder: Boolean
    ) : VaultChange

    data class Deleted(val path: RelativePath) : VaultChange

    data class Modified(val path: RelativePath) : VaultChange

    /** The platform dropped events (too many changes at once); only a full rescan is reliable. */
    data object Overflow : VaultChange
}
