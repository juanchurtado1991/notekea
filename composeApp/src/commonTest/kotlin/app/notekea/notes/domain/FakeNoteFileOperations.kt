package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Files and folders as a set of paths. [failure] makes every change fail; [takenOnCreate] simulates names taken by
 * another app between the check and the creation.
 */
class FakeNoteFileOperations : NoteFileOperations {
    val existing = mutableSetOf<RelativePath>()
    val moves = mutableListOf<Pair<RelativePath, RelativePath>>()
    val takenOnCreate = mutableSetOf<RelativePath>()
    var failure: NotesError? = null

    override suspend fun createFile(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = create(path = path)

    override suspend fun createFolder(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = create(path = path)

    override suspend fun exists(
        vaultPath: String,
        path: RelativePath
    ): Boolean = path in existing

    override suspend fun move(
        vaultPath: String,
        from: RelativePath,
        to: RelativePath
    ): Outcome<Unit, NotesError> {
        failure?.let { return Outcome.Failure(error = it) }
        moves += from to to
        existing -= from
        existing += to
        return Outcome.Success(value = Unit)
    }

    private fun create(
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        failure?.let { return Outcome.Failure(error = it) }
        if (path in takenOnCreate) return Outcome.Failure(error = NotesError.NameTaken(name = path.name))
        existing += path
        return Outcome.Success(value = Unit)
    }
}
