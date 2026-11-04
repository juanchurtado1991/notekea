package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/** Shows a note or folder in the system file manager (Finder, File Explorer, Files…), selected when possible. */
interface FileRevealer {
    suspend fun reveal(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError>
}
