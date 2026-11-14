package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NewNoteDefaults
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.createUnderFreeName

/**
 * Creates a note in `folder` named after `baseName` (the localized "Untitled"), numbered when it is taken: empty but
 * for the properties every new note starts with ([defaults]).
 */
class CreateNoteUseCase(
    private val noteFileOperations: NoteFileOperations,
    private val noteContentStore: NoteContentStore? = null,
    private val defaults: NewNoteDefaults? = null
) {
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
        val created = noteFileOperations.createFile(
            vaultPath = vaultPath,
            path = path
        )

        // Written after the file exists: a note without them is still a note, so a failure here is not the creation's.
        if (created is Outcome.Success && defaults != null) {
            noteContentStore?.write(
                vaultPath = vaultPath,
                path = path,
                content = defaults.header()
            )
        }

        created
    }
}
