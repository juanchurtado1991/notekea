package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/**
 * Moves a note, a file or a folder into `folder` under its own name, as dragging it there in the tree does, and
 * returns its new path. Nothing moves into where it already is; a folder never moves into itself, and a name taken
 * there is said, never written over.
 */
class MoveEntryUseCase(private val noteFileOperations: NoteFileOperations) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        folder: RelativePath
    ): Outcome<RelativePath, NotesError> {
        val target = folder.child(name = path.name)
        val isInside = folder == path || (!folder.isRoot && folder.isWithin(folder = path))

        return when {
            folder == (path.parent ?: RelativePath.ROOT) -> Outcome.Success(value = path)

            isInside -> Outcome.Failure(error = NotesError.InvalidName)

            noteFileOperations.exists(vaultPath = vaultPath, path = target) ->
                Outcome.Failure(error = NotesError.NameTaken(name = path.name))

            else -> when (
                val moved = noteFileOperations.move(vaultPath = vaultPath, from = path, to = target)
            ) {
                is Outcome.Failure -> moved
                is Outcome.Success -> Outcome.Success(value = target)
            }
        }
    }
}
