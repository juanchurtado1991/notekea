package app.notekea.editor.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/** Reads a note for the editor, split into the header it keeps untouched and the body the user edits. */
class OpenNoteUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath
    ): Outcome<NoteDocument, NotesError> {
        return when (val text = noteContentStore.readText(vaultPath = vaultPath, path = path)) {
            is Outcome.Failure -> text
            is Outcome.Success -> Outcome.Success(value = NoteDocument.fromText(text = text.value))
        }
    }
}
