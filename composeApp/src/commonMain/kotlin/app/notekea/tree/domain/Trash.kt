package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/** Deletes notes and folders recoverably: nothing in a vault is ever deleted permanently. */
interface Trash {
    suspend fun moveToTrash(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError>
}
