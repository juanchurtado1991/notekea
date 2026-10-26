package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * The files of a vault other than its notes' text: listed to resolve embeds, read to show them, written or copied to
 * attach new ones. Paths are relative to the vault; nothing is ever overwritten ([NotesError.NameTaken]).
 */
interface AttachmentStore {
    /** Every file of the vault, notes included, outside hidden folders (`.obsidian`, `.trash`). */
    suspend fun files(
        vaultPath: String
    ): Outcome<List<RelativePath>, NotesError>

    suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): Outcome<ByteArray, NotesError>

    /** Saves [bytes] as a new file at [path], creating its folders. */
    suspend fun write(
        vaultPath: String,
        path: RelativePath,
        bytes: ByteArray
    ): Outcome<Unit, NotesError>

    /** Copies the file at [absolutePath] to a new file at [path], creating its folders. */
    suspend fun copy(
        vaultPath: String,
        absolutePath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError>

    /** Where [absolutePath] is inside the vault, or null when it is outside it. */
    fun pathInVault(
        vaultPath: String,
        absolutePath: String
    ): RelativePath?

    /** The file at [path] as a path on the computer, to open it with the system's app. */
    fun absolutePath(
        vaultPath: String,
        path: RelativePath
    ): String
}
