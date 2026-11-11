package app.notekea.core.documents.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath

/** Reads a document of the vault as [DocumentText] (Tika on desktop), the same text while it is unchanged. */
interface DocumentReader {
    suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): Outcome<DocumentText, DocumentError>
}
