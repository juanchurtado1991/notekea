package app.notekea.editor.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteFileOperations
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.createUnderFreeName

/**
 * Keeps [text], the note's full text that could not be saved over [note] (an outside change, a deleted file, a failed
 * write), in a new note beside it, `Name (unsaved)`, numbered if that name is taken, so nothing typed is ever lost.
 */
class KeepUnsavedTextUseCase(
    private val noteFileOperations: NoteFileOperations,
    private val noteContentStore: NoteContentStore
) {
    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath,
        text: String
    ): Outcome<RelativePath, NotesError> = createUnderFreeName(
        noteFileOperations = noteFileOperations,
        vaultPath = vaultPath,
        folder = note.parent ?: RelativePath.ROOT,
        baseName = note.nameWithoutExtension + SUFFIX,
        extension = RelativePath.MARKDOWN_EXTENSION
    ) { path ->
        when (val created = noteFileOperations.createFile(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> created

            is Outcome.Success -> noteContentStore.write(vaultPath = vaultPath, path = path, content = text).also {
                if (it is Outcome.Failure) noteFileOperations.deleteEmptyFile(vaultPath = vaultPath, path = path)
            }
        }
    }

    private companion object {
        const val SUFFIX = " (unsaved)"
    }
}
