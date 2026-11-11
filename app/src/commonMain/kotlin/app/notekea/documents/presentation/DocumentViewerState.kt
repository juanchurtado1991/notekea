package app.notekea.documents.presentation

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.documents.domain.DocumentFormat

/**
 * A document's read-only tab: its [format], its text as [blocks] (or, for a PDF, [pageCount] real pages, those drawn
 * so far in [pages]), the passage [shown], and why it cannot be read ([error]).
 */
data class DocumentViewerState(
    val name: String = "",
    val format: DocumentFormat? = null,
    val isLoading: Boolean = true,
    val error: DocumentError? = null,
    val blocks: List<DocumentBlock> = emptyList(),
    val pageCount: Int = 0,
    val pages: Map<Int, ImageBitmap> = emptyMap(),
    val shown: ShownPassage? = null
) {
    val isPaged: Boolean get() = format?.isPaged == true && pageCount > 0
}
