package app.notekea.editor.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/**
 * Writes the editor's [NoteDocument] back to its note, never over a change made elsewhere: when the file no longer
 * holds `expectedOnDisk` nothing is written ([NotesError.ChangedOnDisk]); a file that is gone is not recreated and one
 * that cannot be read is not replaced. Null skips the check ("Restore"). Returns the text written.
 */
class SaveNoteUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        document: NoteDocument,
        expectedOnDisk: String?
    ): Outcome<String, NotesError> {
        if (expectedOnDisk != null) {
            val onDisk = when (
                val read = noteContentStore.readText(
                    vaultPath = vaultPath,
                    path = path
                )
            ) {
                is Outcome.Failure -> return read
                is Outcome.Success -> read.value
            }

            if (onDisk != expectedOnDisk) {
                return Outcome.Failure(error = NotesError.ChangedOnDisk(path = path.value))
            }
        }

        val text = document.toText()

        return when (
            val written = noteContentStore.write(
                vaultPath = vaultPath,
                path = path,
                content = text
            )
        ) {
            is Outcome.Failure -> written
            is Outcome.Success -> Outcome.Success(value = text)
        }
    }
}
