package app.notekea.notes.domain

/** Expected failures while reading or changing the notes of a vault; each one has a plain-language message. */
sealed interface NotesError {
    /** The note or folder was moved or deleted outside Notekea before the action ran. */
    data class EntryMissing(val path: String) : NotesError

    data class NameTaken(val name: String) : NotesError

    /** The file system refused the change (read-only disk, permissions, file in use…). */
    data class OperationFailed(val path: String) : NotesError

    data class PermissionDenied(val path: String) : NotesError

    data class VaultMissing(val path: String) : NotesError
}
