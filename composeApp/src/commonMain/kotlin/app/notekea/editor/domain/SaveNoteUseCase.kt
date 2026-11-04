package app.notekea.editor.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/**
 * Writes the editor's [NoteDocument] back to its note, never over a change made elsewhere: `expectedOnDisk` is the
 * text the editor last read or wrote, and when the file now holds something else (another app, a sync) nothing is
 * written and [NotesError.ChangedOnDisk] lets the user choose. A file that is gone (moved, renamed or deleted
 * meanwhile) is never recreated by a save: [NotesError.EntryMissing], and a file that cannot be read is never replaced
 * by one: [NotesError.Unreadable]. Passing the file's current text overwrites it on
 * purpose ("Keep mine"); null skips the check, for a note the user brings back ("Restore"). Returns the text written,
 * the next save's `expectedOnDisk`.
 */
class SaveNoteUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath,
        document: NoteDocument,
        expectedOnDisk: String?
    ): Outcome<String, NotesError> {
        if (expectedOnDisk != null) {
            val onDisk = when (val read = noteContentStore.readText(vaultPath = vaultPath, path = path)) {
                is Outcome.Failure -> return read
                is Outcome.Success -> read.value
            }
            if (onDisk != expectedOnDisk) return Outcome.Failure(error = NotesError.ChangedOnDisk(path = path.value))
        }
        val text = document.toText()
        return when (val written = noteContentStore.write(vaultPath = vaultPath, path = path, content = text)) {
            is Outcome.Failure -> written
            is Outcome.Success -> Outcome.Success(value = text)
        }
    }
}
