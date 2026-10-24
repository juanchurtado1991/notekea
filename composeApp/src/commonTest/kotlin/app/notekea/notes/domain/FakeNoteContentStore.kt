package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Note texts in memory; writes to [refused] paths fail as a locked file would. */
class FakeNoteContentStore : NoteContentStore {
    val texts = mutableMapOf<RelativePath, String>()
    val refused = mutableSetOf<RelativePath>()

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = texts[path]

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> {
        if (path in refused) return Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        texts[path] = content
        return Outcome.Success(value = Unit)
    }
}
