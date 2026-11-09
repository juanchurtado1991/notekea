package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.Gate
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/** Records what was trashed and answers with [outcome]; every call goes through [gate], as the disk takes its time. */
class FakeTrash(
    var outcome: Outcome<TrashLocation, NotesError> = Outcome.Success(
        value = TrashLocation.VAULT_TRASH_FOLDER
    )
) :
    Trash {
    val trashed = mutableListOf<RelativePath>()
    val gate = Gate()

    override suspend fun moveToTrash(
        vaultPath: String,
        path: RelativePath
    ): Outcome<TrashLocation, NotesError> {
        gate.pass()
        trashed += path

        return outcome
    }
}
