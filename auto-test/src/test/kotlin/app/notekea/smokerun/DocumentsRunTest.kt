package app.notekea.smokerun

import androidx.compose.ui.test.ExperimentalTestApi
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File
import kotlin.test.Test

/**
 * The documents run of 4.8: large documents dropped into the open Sample Vault (a PDF of 300 pages and an HTML page of
 * 2 MB of text), the time until they are ready to search, and memory once they are and after the app sits idle.
 */
@OptIn(ExperimentalTestApi::class)
class DocumentsRunTest {
    @Test
    fun `large documents are indexed in little time and memory`() = smokeRun(name = RUN) { app ->
        openVault(
            app = app,
            name = SAMPLE_VAULT,
            shownNote = HOME
        )

        val vault = File(app.vaultPath(name = SAMPLE_VAULT))
        val notes = readableIn(vault = vault)

        waitForText(
            text = "$notes notes ready to search",
            substring = true,
            timeoutMillis = INDEX_MILLIS
        )

        SmokeRunReport.sample(
            run = RUN,
            moment = "notes-indexed"
        )

        val start = System.nanoTime()
        pdf(file = File(vault, "Large manual.pdf"))
        File(vault, "Large page.html").writeText(text = html())

        waitForText(
            text = "${notes + 2} notes ready to search",
            substring = true,
            timeoutMillis = INDEX_MILLIS
        )

        println("Documents ready to search in ${(System.nanoTime() - start) / NANOS_PER_MILLI} ms")

        SmokeRunReport.sample(
            run = RUN,
            moment = "documents-indexed"
        )

        val idle = SmokeRunReport.idleSeconds

        if (idle > 0) {
            Thread.sleep(idle * MILLIS)

            SmokeRunReport.sample(
                run = RUN,
                moment = "idle-${idle}s"
            )
        }
    }

    // What the index reads, as the tree lists it: notes and documents outside hidden folders.
    private fun readableIn(vault: File): Int = vault
        .walkTopDown()
        .onEnter {
            !it.name.startsWith(prefix = ".")
        }
        .count {
            it.isFile && it.extension.lowercase() in READABLE
        }

    private fun pdf(file: File) = PDDocument().use { document ->
        repeat(times = PDF_PAGES) { number ->
            val page = PDPage()
            document.addPage(page)

            PDPageContentStream(document, page).use { content ->
                content.beginText()
                content.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE)
                content.setLeading(LEADING)
                content.newLineAtOffset(MARGIN, TOP)

                repeat(times = PDF_LINES) { line ->
                    content.showText("Page ${number + 1}, line ${line + 1}: the pump is serviced every spring.")
                    content.newLine()
                }

                content.endText()
            }
        }

        document.save(file)
    }

    private fun html(): String = buildString {
        append("<html><head><title>Large page</title></head><body>")

        repeat(times = HTML_PARAGRAPHS) {
            append("<h2>Section ${it + 1}</h2><p>")
            append(PARAGRAPH.repeat(n = PARAGRAPH_SENTENCES))
            append("</p>")
        }

        append("</body></html>")
    }

    private companion object {
        const val RUN = "documents"
        const val HOME = "Home"
        const val INDEX_MILLIS = 600_000L
        const val MILLIS = 1_000L
        const val NANOS_PER_MILLI = 1_000_000L
        const val PDF_PAGES = 300
        const val PDF_LINES = 40
        const val HTML_PARAGRAPHS = 1_100
        const val PARAGRAPH_SENTENCES = 25
        const val PARAGRAPH =
            "The river rises in spring and the ferries run every hour along the coast. "
        const val FONT_SIZE = 10f
        const val LEADING = 14f
        const val MARGIN = 54f
        const val TOP = 740f
        val READABLE =
            setOf("md", "pdf", "docx", "odt", "epub", "pptx", "xlsx", "html", "htm", "txt", "rst", "org", "csv", "json")
    }
}
