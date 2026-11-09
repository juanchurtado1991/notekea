package app.notekea.core.notes.domain

/** Why the search index could not be used; never what the notes say (`GUIDELINES.md` §13). */
sealed interface IndexError {
    /** Another Notekea holds the vault's index (the vault is open twice): this one waits and tries again. */
    data object Busy : IndexError

    /** The index folder cannot be written or read (a full disk, permissions, a damaged index). */
    data object Unavailable : IndexError
}
