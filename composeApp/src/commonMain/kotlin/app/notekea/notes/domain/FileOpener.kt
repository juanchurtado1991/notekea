package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Opens a vault's file with the app the system uses for it (a PDF viewer, a music player…). */
interface FileOpener {
    suspend fun open(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError>
}
