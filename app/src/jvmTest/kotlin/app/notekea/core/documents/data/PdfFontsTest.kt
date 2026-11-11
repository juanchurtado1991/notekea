package app.notekea.core.documents.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A PDF read without PDFBox scanning the computer's fonts, nor writing its font cache anywhere. Run alone it proves
 * it; after other tests built PDFs with PDFBox in the same JVM, the scan has already happened elsewhere.
 */
class PdfFontsTest {
    // CI of 2026-11-11: on Windows and macOS PDFBox's first look at a font scanned every system font, slowing the
    // Playground Vault's opening, whose attachments hold a PDF, until Auto-Test cases timed out.
    @Test
    fun `a PDF whose font is not embedded is read with no font scan and no cache written`() =
        runBlocking {
            val vault = Files.createTempDirectory("notekea-fonts").toFile()
            val cacheFolder = Files.createTempDirectory("notekea-font-cache").toFile()
            System.setProperty(CACHE_PROPERTY, cacheFolder.path)

            // Made outside PDFBox: building a PDF with PDFBox would look its font up first.
            val bytes = PdfFontsTest::class
                .java
                .getResourceAsStream("/documents/$PDF")!!
                .readBytes()

            File(vault, PDF).writeBytes(bytes)

            val read = TikaDocumentReader(dispatchers = JvmAppDispatchers()).read(
                vaultPath = vault.path,
                path = RelativePath(value = PDF)
            )

            assertEquals(
                expected = listOf<Any?>(true, emptyList<String>()),
                actual = listOf(
                    read is Outcome.Success,
                    cacheFolder
                        .list()
                        .orEmpty()
                        .toList()
                )
            )
        }

    private companion object {
        const val PDF = "Unembedded font.pdf"
        const val CACHE_PROPERTY = "pdfbox.fontcache"
    }
}
