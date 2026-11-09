package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.createUnderFreeName

/** Creates an empty folder in `folder` named after `baseName` (the localized "Untitled folder"), numbered when taken. */
class CreateFolderUseCase(private val noteFileOperations: NoteFileOperations) {
    suspend operator fun invoke(
        vaultPath: String,
        folder: RelativePath,
        baseName: String
    ): Outcome<RelativePath, NotesError> = createUnderFreeName(
        noteFileOperations = noteFileOperations,
        vaultPath = vaultPath,
        folder = folder,
        baseName = baseName,
        extension = ""
    ) { path -> noteFileOperations.createFolder(vaultPath = vaultPath, path = path) }
}
