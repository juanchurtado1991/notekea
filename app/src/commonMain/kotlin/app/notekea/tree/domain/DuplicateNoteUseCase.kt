package app.notekea.tree.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.createUnderFreeName

/**
 * Makes a copy of a note beside it, named after it with `suffix` (the localized "copy"), numbered when that is
 * taken, and returns the copy's path. The note itself is only read.
 */
class DuplicateNoteUseCase(
    private val noteFileOperations: NoteFileOperations,
    private val noteContentStore: NoteContentStore
) {
    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath,
        suffix: String
    ): Outcome<RelativePath, NotesError> {
        val text = noteContentStore.read(
            vaultPath = vaultPath,
            path = note
        ) ?: return Outcome.Failure(error = NotesError.EntryMissing(path = note.value))

        return createUnderFreeName(
            noteFileOperations = noteFileOperations,
            vaultPath = vaultPath,
            folder = note.parent ?: RelativePath.ROOT,
            baseName = "${note.nameWithoutExtension} $suffix",
            extension = RelativePath.MARKDOWN_EXTENSION
        ) { path ->
            val created = noteFileOperations.createFile(
                vaultPath = vaultPath,
                path = path
            )

            if (created is Outcome.Failure) {
                created
            } else {
                noteContentStore.write(
                    vaultPath = vaultPath,
                    path = path,
                    content = text
                )
            }
        }
    }
}
