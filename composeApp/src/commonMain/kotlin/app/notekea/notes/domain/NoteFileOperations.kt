package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Changes to the files and folders of a vault, each one started by the user. Paths are relative to the vault;
 * nothing is ever overwritten: a name already in use fails with [NotesError.NameTaken].
 */
interface NoteFileOperations {
    /** Creates an empty file at [path]. Its folder must exist. */
    suspend fun createFile(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError>

    /** Creates an empty folder at [path]. Its parent must exist. */
    suspend fun createFolder(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError>

    /** Whether anything (file or folder, hidden or not) exists at [path]. */
    suspend fun exists(
        vaultPath: String,
        path: RelativePath
    ): Boolean

    /** Renames or moves a file or folder with everything inside it. Changing only the letter case is allowed. */
    suspend fun move(
        vaultPath: String,
        from: RelativePath,
        to: RelativePath
    ): Outcome<Unit, NotesError>
}
