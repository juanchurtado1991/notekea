package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Deletes notes and folders recoverably: nothing in a vault is ever deleted permanently. */
interface Trash {
    suspend fun moveToTrash(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError>
}
