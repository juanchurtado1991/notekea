package app.notekea.core.documents.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.documents.domain.DocumentPartKind
import app.notekea.core.documents.domain.DocumentText
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Every format a vault may hold read by Tika, with the sentence of each found, and what cannot be read told apart. */
class TikaDocumentReaderTest {
    private val vault = Files.createTempDirectory("notekea-documents").toFile()
    private val reader = TikaDocumentReader(dispatchers = JvmAppDispatchers())

    @AfterTest
    fun tearDown() {
        vault.deleteRecursively()
    }

    private fun read(name: String) = runBlocking {
        reader.read(
            vaultPath = vault.path,
            path = RelativePath(value = name)
        )
    }

    private fun text(name: String): DocumentText = (read(name = name) as Outcome.Success).value

    @Test
    fun `a sentence of each format is found in its text`() {
        DocumentFixtures.pdf(file = File(vault, "Guide.pdf"))
        DocumentFixtures.docx(file = File(vault, "Guide.docx"))
        DocumentFixtures.xlsx(file = File(vault, "Budget.xlsx"))
        DocumentFixtures.pptx(file = File(vault, "Talk.pptx"))
        DocumentFixtures.html(file = File(vault, "Guide.html"))
        DocumentFixtures.epub(file = File(vault, "Guide.epub"))
        DocumentFixtures.odt(file = File(vault, "Guide.odt"))
        File(vault, "Notes.txt").writeText("Plain text. ${DocumentFixtures.SENTENCE}\n")

        val names =
            listOf("Guide.pdf", "Guide.docx", "Talk.pptx", "Guide.html", "Guide.epub", "Guide.odt", "Notes.txt")

        val missing = names.filterNot {
            DocumentFixtures.SENTENCE in text(name = it).text
        }

        val budget = text(name = "Budget.xlsx").text

        assertEquals(
            expected = emptyList<String>() to listOf(true, true),
            actual = missing to listOf("## Sheet: Budget" in budget, "| Tram pass | 40 |" in budget)
        )
    }

    @Test
    fun `a PDF's sentence is on its page, and a slide on its slide`() {
        DocumentFixtures.pdf(file = File(vault, "Guide.pdf"))
        DocumentFixtures.pptx(file = File(vault, "Talk.pptx"))
        val pdf = text(name = "Guide.pdf")
        val talk = text(name = "Talk.pptx")

        val page = pdf.partAt(offset = pdf.text.indexOf(string = DocumentFixtures.SENTENCE))
        val slide = talk.partAt(offset = talk.text.indexOf(string = DocumentFixtures.SENTENCE))

        assertEquals(
            expected = listOf(DocumentPartKind.PAGE to 2, DocumentPartKind.SLIDE to 1),
            actual = listOf(page?.kind to page?.number, slide?.kind to slide?.number)
        )
    }

    @Test
    fun `a damaged file, a scan without text and a missing file are each told apart`() {
        File(vault, "Broken.pdf").writeBytes(ByteArray(size = 2_000) { it.toByte() })

        DocumentFixtures.pdf(
            file = File(vault, "Scan.pdf"),
            isBlank = true
        )

        val broken = read(name = "Broken.pdf")

        assertTrue(actual = broken is Outcome.Failure && broken.error is DocumentError.Unreadable)

        assertEquals(
            expected = listOf<Any?>(DocumentError.NoText, DocumentError.Missing),
            actual = listOf(
                (read(name = "Scan.pdf") as Outcome.Failure).error,
                (read(name = "Gone.pdf") as Outcome.Failure).error
            )
        )
    }

    @Test
    fun `a document changed on disk is read again`() {
        val file = File(vault, "Notes.txt")
        file.writeText("first\n")
        val before = text(name = "Notes.txt").text
        file.writeText("second, longer\n")
        file.setLastModified(file.lastModified() + 2_000)

        assertEquals(
            expected = "first\n" to "second, longer\n",
            actual = before to text(name = "Notes.txt").text
        )
    }
}
