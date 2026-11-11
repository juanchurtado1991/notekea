package app.notekea.core.documents.data

import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.documents.domain.DocumentReader
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteContentStore
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath

/**
 * The vault's text for whoever reads it (the index, the chat, search snippets): a note as it is stored ([notes]), a
 * document as the Markdown [documents] read out of it (4.5). A document is never written: Notekea only reads them.
 */
class DocumentAwareContentStore(
    private val notes: NoteContentStore,
    private val documents: DocumentReader
) : NoteContentStore {
    override suspend fun readText(
        vaultPath: String,
        path: RelativePath
    ): Outcome<String, NotesError> {
        if (DocumentFormat.of(path = path) == null) {
            return notes.readText(
                vaultPath = vaultPath,
                path = path
            )
        }

        return when (val read = documents.read(vaultPath = vaultPath, path = path)) {
            is Outcome.Success -> Outcome.Success(value = read.value.text)
            is Outcome.Failure -> Outcome.Failure(error = NotesError.OpenFailed(path = path.value))
        }
    }

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): String? = (
        readText(
            vaultPath = vaultPath,
            path = path
        ) as? Outcome.Success
        )?.value

    override suspend fun write(
        vaultPath: String,
        path: RelativePath,
        content: String
    ): Outcome<Unit, NotesError> {
        val isDocument = DocumentFormat.of(path = path) != null
        val refused = NotesError.OperationFailed(path = path.value)
        if (isDocument) return Outcome.Failure(error = refused)

        return notes.write(
            vaultPath = vaultPath,
            path = path,
            content = content
        )
    }
}
