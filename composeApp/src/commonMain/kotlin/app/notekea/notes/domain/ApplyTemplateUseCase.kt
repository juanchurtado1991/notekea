package app.notekea.notes.domain

import app.notekea.core.domain.Outcome

/**
 * Fills the note at [note] on disk from [template] (`ROADMAP.md` 1.12), for a note made with "New note from template":
 * the template is filled in with the note's name as its title, then inserted ([TemplateInsertion]), so an empty note
 * becomes the template and a note with text gets it at its end.
 */
class ApplyTemplateUseCase(
    private val fillTemplate: FillTemplateUseCase,
    private val noteContentStore: NoteContentStore
) {
    private val insertion = TemplateInsertion()

    suspend operator fun invoke(
        vaultPath: String,
        note: RelativePath,
        template: RelativePath
    ): Outcome<Unit, NotesError> {
        val filled = when (val outcome = fillTemplate(vaultPath, template, title = note.nameWithoutExtension)) {
            is Outcome.Failure -> return outcome
            is Outcome.Success -> outcome.value
        }
        val text = noteContentStore.read(vaultPath = vaultPath, path = note)
            ?: return Outcome.Failure(error = NotesError.EntryMissing(path = note.value))
        val document = NoteDocument.fromText(text = text)
        val end = document.body.length
        val result = insertion.insert(note = document, selectionStart = end, selectionEnd = end, template = filled)
        val body = document.body.replaceRange(
            startIndex = result.edit.start,
            endIndex = result.edit.end,
            replacement = result.edit.replacement
        )
        val content = document.copy(header = result.header, body = body).toText()
        return noteContentStore.write(vaultPath = vaultPath, path = note, content = content)
    }
}
