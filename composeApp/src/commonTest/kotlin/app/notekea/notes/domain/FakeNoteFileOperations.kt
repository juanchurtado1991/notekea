package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Files and folders as a set of paths. [failure] makes every change fail; [takenOnCreate] simulates names taken by
 * another app between the check and the creation; [onMove] lets what a test holds about the files follow a move. Every
 * call goes through [gate], as the disk takes its time.
 */
class FakeNoteFileOperations : NoteFileOperations {
    val existing = mutableSetOf<RelativePath>()
    val moves = mutableListOf<Pair<RelativePath, RelativePath>>()
    val takenOnCreate = mutableSetOf<RelativePath>()
    var failure: NotesError? = null
    val gate = Gate()
    var onMove: (RelativePath, RelativePath) -> Unit = { _, _ -> }

    override suspend fun createFile(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = create(path = path)

    override suspend fun deleteEmptyFile(
        vaultPath: String,
        path: RelativePath
    ) {
        gate.pass()
        existing -= path
    }

    override suspend fun createFolder(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Unit, NotesError> = create(path = path)

    override suspend fun exists(
        vaultPath: String,
        path: RelativePath
    ): Boolean {
        gate.pass()
        return path in existing
    }

    override suspend fun move(
        vaultPath: String,
        from: RelativePath,
        to: RelativePath
    ): Outcome<Unit, NotesError> {
        gate.pass()
        failure?.let { return Outcome.Failure(error = it) }
        moves += from to to
        existing -= from
        existing += to
        onMove(from, to)
        return Outcome.Success(value = Unit)
    }

    private suspend fun create(
        path: RelativePath
    ): Outcome<Unit, NotesError> {
        gate.pass()
        failure?.let { return Outcome.Failure(error = it) }
        if (path in takenOnCreate) return Outcome.Failure(error = NotesError.NameTaken(name = path.name))
        existing += path
        return Outcome.Success(value = Unit)
    }
}
