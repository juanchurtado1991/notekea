package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/** Reads a note for the editor, split into the header it keeps untouched and the body the user edits. */
class OpenNoteUseCase(private val noteContentStore: NoteContentStore) {
    suspend operator fun invoke(
        vaultPath: String,
        path: RelativePath
    ): Outcome<NoteDocument, NotesError> {
        val text = noteContentStore.read(vaultPath = vaultPath, path = path)
            ?: return Outcome.Failure(error = NotesError.OperationFailed(path = path.value))
        return Outcome.Success(value = NoteDocument.fromText(text = text))
    }
}
