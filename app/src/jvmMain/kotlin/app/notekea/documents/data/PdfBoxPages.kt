package app.notekea.documents.data

import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.documents.domain.PageMark
import app.notekea.documents.domain.PdfPages
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * [PdfPages] with PDFBox: the PDF open while its tab reads it (one at a time, closed when another is asked for), each
 * page drawn to a PNG, and a passage found on a page by its text positions ([PageTextPositions]).
 */
class PdfBoxPages(private val dispatchers: AppDispatchers) : PdfPages {
    private val lock = Mutex()
    private var open: Pair<String, PDDocument>? = null

    override suspend fun count(
        vaultPath: String,
        path: RelativePath
    ): Outcome<Int, DocumentError> = withDocument(
        vaultPath = vaultPath,
        path = path
    ) { it.numberOfPages }

    override suspend fun render(
        vaultPath: String,
        path: RelativePath,
        page: Int,
        scale: Float
    ): Outcome<ByteArray, DocumentError> = withDocument(
        vaultPath = vaultPath,
        path = path
    ) { document ->
        val image = PDFRenderer(document).renderImage(page, scale)

        ByteArrayOutputStream().use { bytes ->
            ImageIO.write(image, PNG, bytes)
            bytes.toByteArray()
        }
    }

    override suspend fun marks(
        vaultPath: String,
        path: RelativePath,
        page: Int,
        passage: String
    ): List<PageMark> {
        val found = withDocument(
            vaultPath = vaultPath,
            path = path
        ) { document ->
            PageTextPositions.marksOf(
                document = document,
                page = page,
                passage = passage
            )
        }

        return (found as? Outcome.Success)?.value.orEmpty()
    }

    private suspend fun <T> withDocument(
        vaultPath: String,
        path: RelativePath,
        block: (PDDocument) -> T
    ): Outcome<T, DocumentError> = withContext(context = dispatchers.io) {
        lock.withLock {
            try {
                Outcome.Success(value = block(documentOf(file = File(vaultPath, path.value))))
            } catch (exception: Exception) {
                if (exception is kotlinx.coroutines.CancellationException) throw exception
                val message = exception.message ?: exception.toString()
                Outcome.Failure(error = DocumentError.Unreadable(message = message))
            }
        }
    }

    private fun documentOf(file: File): PDDocument {
        val key = "${file.absolutePath}|${file.length()}|${file.lastModified()}"

        open?.takeIf {
            it.first == key
        }?.let {
            return it.second
        }

        open?.second?.close()
        val document = Loader.loadPDF(file)
        open = key to document

        return document
    }

    private companion object {
        const val PNG = "png"
    }
}
