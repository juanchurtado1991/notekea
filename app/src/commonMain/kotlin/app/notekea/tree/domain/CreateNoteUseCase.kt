package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.createUnderFreeName

/** Creates an empty note in `folder` named after `baseName` (the localized "Untitled"), numbered when it is taken. */
class CreateNoteUseCase(private val noteFileOperations: NoteFileOperations) {
    suspend operator fun invoke(
        vaultPath: String,
        folder: RelativePath,
        baseName: String
    ): Outcome<RelativePath, NotesError> = createUnderFreeName(
        noteFileOperations = noteFileOperations,
        vaultPath = vaultPath,
        folder = folder,
        baseName = baseName,
        extension = RelativePath.MARKDOWN_EXTENSION
    ) { path ->
        noteFileOperations.createFile(
            vaultPath = vaultPath,
            path = path
        )
    }
}
