package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Shows a note or folder in the system file manager (Finder, File Explorer, Files…), selected when possible. */
interface FileRevealer {
    suspend fun reveal(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError>
}
