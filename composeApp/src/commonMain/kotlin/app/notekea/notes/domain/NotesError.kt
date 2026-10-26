package app.notekea.notes.domain

/** Expected failures while reading or changing the notes of a vault; each one has a plain-language message. */
sealed interface NotesError {
    /** The note changed outside Notekea since the editor last read or saved it, so saving would overwrite that. */
    data class ChangedOnDisk(val path: String) : NotesError

    /** The clipboard offers a copied image but did not give it in time. */
    data object ClipboardImageUnreadable : NotesError

    /** A copied image can only be read on this desktop with [tool], which is not installed. */
    data class ClipboardToolMissing(val tool: String) : NotesError

    /** The note or folder was moved or deleted outside Notekea before the action ran. */
    data class EntryMissing(val path: String) : NotesError

    /** A name starting with a dot would hide the entry from the tree (and from Obsidian). */
    data object HiddenName : NotesError

    /** A name that cannot be a file on every supported system (empty, `..`, or with `/ \ : * ? " < > |`). */
    data object InvalidName : NotesError

    data class NameTaken(val name: String) : NotesError

    /** The file system refused the change (read-only disk, permissions, file in use…). */
    data class OperationFailed(val path: String) : NotesError

    /** A file could not be opened with the system's app for it. */
    data class OpenFailed(val path: String) : NotesError

    data class PermissionDenied(val path: String) : NotesError

    /** The system file manager could not be opened on the entry. */
    data class RevealFailed(val path: String) : NotesError

    data class VaultMissing(val path: String) : NotesError
}
