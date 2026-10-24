package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Records what was trashed and answers with [outcome]. */
class FakeTrash(
    var outcome: Outcome<TrashLocation, NotesError> = Outcome.Success(value = TrashLocation.VAULT_TRASH_FOLDER)
) :
    Trash {
    val trashed = mutableListOf<RelativePath>()

    override suspend fun moveToTrash(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError> {
        trashed += path
        return outcome
    }
}
