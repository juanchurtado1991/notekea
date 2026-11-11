package app.notekea.core.documents.data

import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.documents.domain.DocumentReader
import app.notekea.core.documents.domain.DocumentText
import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import java.io.File

/**
 * [DocumentReader] with Apache Tika: the document's XHTML as Markdown ([MarkdownContentHandler]); plain text read as
 * it is. The last few documents read are kept while unchanged on disk, so the index, the chat and the viewer read a
 * PDF once.
 */
class TikaDocumentReader(private val dispatchers: AppDispatchers) : DocumentReader {
    private val parser by lazy {
        PdfFonts.useBundledOnly()
        AutoDetectParser()
    }
    private val lock = Mutex()
    private val recent = LinkedHashMap<String, DocumentText>(RECENT, LOAD_FACTOR, true)

    override suspend fun read(
        vaultPath: String,
        path: RelativePath
    ): Outcome<DocumentText, DocumentError> = withContext(context = dispatchers.io) {
        val file = File(vaultPath, path.value)
        val format = DocumentFormat.of(path = path)

        when {
            !file.isFile -> Outcome.Failure(error = DocumentError.Missing)

            format == null -> unreadable(message = NOT_A_DOCUMENT)

            file.length() > MAX_BYTES -> unreadable(message = TOO_LARGE)

            else -> cached(
                file = file,
                format = format
            )
        }
    }

    private suspend fun cached(
        file: File,
        format: DocumentFormat
    ): Outcome<DocumentText, DocumentError> {
        val key = "${file.absolutePath}|${file.length()}|${file.lastModified()}"

        lock.withLock {
            recent[key]
        }?.let {
            return Outcome.Success(value = it)
        }

        val read = try {
            parsed(
                file = file,
                format = format
            )
        } catch (exception: Exception) {
            if (exception is kotlinx.coroutines.CancellationException) throw exception

            return unreadable(message = exception.message ?: exception.toString())
        }

        if (!hasWords(read = read)) return Outcome.Failure(error = DocumentError.NoText)

        lock.withLock {
            recent[key] = read
            while (recent.size > RECENT) recent.remove(recent.keys.first())
        }

        return Outcome.Success(value = read)
    }

    // A scan has pages and no words: only the headings of its pages.
    private fun hasWords(read: DocumentText): Boolean {
        val headings = read
            .parts
            .map { it.range.first }
            .toSet()

        var offset = 0

        return read
            .text
            .lines()
            .any { line ->
                val isHeading = offset in headings
                offset += line.length + 1

                !isHeading && line.isNotBlank()
            }
    }

    private fun parsed(
        file: File,
        format: DocumentFormat
    ): DocumentText {
        val isPlain = format == DocumentFormat.TEXT
        if (isPlain) return DocumentText(text = plainText(file = file))

        val handler = MarkdownContentHandler(
            format = format,
            maxChars = MAX_CHARS
        )

        val metadata = Metadata()

        try {
            TikaInputStream.get(file.toPath()).use { stream ->
                parser.parse(stream, handler, metadata, ParseContext())
            }
        } catch (exception: Exception) {
            // The handler stops a long document at its limit: what it read so far is the document's text.
            if (!handler.isFull) throw exception
        }

        // Bytes Tika does not recognize at all (a damaged file, or another kind renamed) are no document.
        check(metadata.get(CONTENT_TYPE) != UNKNOWN_TYPE) { NOT_RECOGNIZED }

        return handler.result()
    }

    // At most MAX_CHARS of a text file, read no further.
    private fun plainText(file: File): String = file.bufferedReader().use { reader ->
        val chars = CharArray(size = MAX_CHARS)
        var read = 0

        while (read < MAX_CHARS) {
            val count = reader.read(chars, read, MAX_CHARS - read)
            if (count < 0) break
            read += count
        }

        String(chars, 0, read)
    }

    private fun unreadable(message: String): Outcome<DocumentText, DocumentError> =
        Outcome.Failure(error = DocumentError.Unreadable(message = message))

    private companion object {
        const val RECENT = 8
        const val LOAD_FACTOR = 0.75f
        const val MAX_BYTES = 100L * 1024 * 1024
        const val MAX_CHARS = 2_000_000
        const val NOT_A_DOCUMENT = "Not a document Notekea reads"
        const val NOT_RECOGNIZED = "Damaged, or not what its name says"
        const val CONTENT_TYPE = "Content-Type"
        const val UNKNOWN_TYPE = "application/octet-stream"
        const val TOO_LARGE = "Larger than 100 MB"
    }
}
