package app.notekea.documents.data

import app.notekea.core.documents.data.PdfFonts
import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import app.notekea.documents.domain.PageMark
import app.notekea.documents.domain.PdfPages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
 * [PdfPages] with PDFBox: the PDFs open while their tabs read them (a few at a time, the one read longest ago closed
 * when another is asked for, each when its tab closes), each page drawn to a PNG, and a passage found on a page by
 * its text positions ([PageTextPositions]).
 */
class PdfBoxPages(private val dispatchers: AppDispatchers) : PdfPages {
    private val lock = Mutex()

    // Where a release closes the PDF, after any page being drawn: a closed tab does not wait for it.
    private val scope = CoroutineScope(context = SupervisorJob() + dispatchers.io)

    /** Whether a PDF is open now. */
    internal val isOpen: Boolean get() = open.isNotEmpty()

    // The few PDFs last read, by file, size and date: two tabs side by side closed each other's at every page
    // (the code review of 2026-11-13). The one read longest ago is closed when another is asked for.
    private val open = LinkedHashMap<String, PDDocument>(MOST_OPEN, LOAD_FACTOR, true)

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

    override fun release(
        vaultPath: String,
        path: RelativePath
    ) {
        val file = File(vaultPath, path.value).absolutePath

        scope.launch {
            lock.withLock {
                open
                    .keys
                    .filter { it.startsWith(prefix = "$file|") }
                    .forEach { open.remove(it)?.close() }
            }
        }
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

        open[key]?.let {
            return it
        }

        if (open.size >= MOST_OPEN) {
            val oldest = open.keys.first()
            open.remove(oldest)?.close()
        }

        PdfFonts.useBundledOnly()
        val document = Loader.loadPDF(file)
        open[key] = document

        return document
    }

    private companion object {
        const val PNG = "png"
        const val MOST_OPEN = 3
        const val LOAD_FACTOR = 0.75f
    }
}
