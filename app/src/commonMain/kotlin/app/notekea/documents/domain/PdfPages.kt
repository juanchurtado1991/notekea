package app.notekea.documents.domain

import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath

/** A PDF of the vault as real pages (PDFBox on desktop): how many, each as a PNG, and where a passage is on one. */
interface PdfPages {
    suspend fun count(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Int, DocumentError>

    /** Page [page] (from 0) as a PNG at [scale] (1 is 72 dots per inch). */
    suspend fun render(
        vaultPath: String,
        path: RelativePath,
        page: Int,
        scale: Float
    ): Outcome<ByteArray, DocumentError>

    /** Where [passage] lies on page [page]: one mark per line it covers; none when it is not found there. */
    suspend fun marks(
        vaultPath: String,
        path: RelativePath,
        page: Int,
        passage: String
    ): List<PageMark>
}
