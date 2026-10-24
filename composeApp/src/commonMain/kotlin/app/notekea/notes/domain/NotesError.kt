package app.notekea.notes.domain

/** Expected failures while reading or changing the notes of a vault; each one has a plain-language message. */
sealed interface NotesError {
    data class PermissionDenied(val path: String) : NotesError

    data class VaultMissing(val path: String) : NotesError
}
