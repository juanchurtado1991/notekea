package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Lists the notes of a vault without reading their content. Hidden files and folders (`.git`, `.obsidian`, `.trash`,
 * anything starting with a dot) are skipped, and so are symbolic links, so a link back up the tree cannot loop.
 */
interface NoteScanner {
    suspend fun scan(
        vaultPath: String
    ): Outcome<VaultListing, NotesError>

    /** The current state of one note, or null when it no longer exists or is not a visible Markdown file. */
    suspend fun stat(
        vaultPath: String,
        path: RelativePath
    ): NoteFile?
}
