package app.notekea.documents.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.documents.data.DocumentFixtures
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A PDF's real pages: counted, drawn, and a cited sentence marked on its page only. */
class PdfBoxPagesTest {
    private val vault = Files.createTempDirectory("notekea-pdf").toFile()
    private val pages = PdfBoxPages(dispatchers = JvmAppDispatchers())
    private val path = RelativePath(value = "Guide.pdf")

    @AfterTest
    fun tearDown() {
        vault.deleteRecursively()
    }

    @Test
    fun `pages are counted and drawn, and the sentence is marked on its page`() = runBlocking {
        DocumentFixtures.pdf(file = File(vault, path.value))

        val count = (
            pages.count(
                vaultPath = vault.path,
                path = path
            ) as Outcome.Success
            ).value

        val png = (
            pages.render(
                vaultPath = vault.path,
                path = path,
                page = 1,
                scale = 1f
            ) as Outcome.Success
            ).value

        val image = ImageIO.read(png.inputStream())

        val onItsPage = pages.marks(
            vault.path,
            path,
            page = 1,
            passage = DocumentFixtures.SENTENCE
        )

        val elsewhere = pages.marks(
            vault.path,
            path,
            page = 0,
            passage = DocumentFixtures.SENTENCE
        )

        val mark = onItsPage.single()

        assertEquals(
            expected = listOf<Any?>(2, 612, true, true, 0),
            actual = listOf(
                count,
                image.width,
                mark.left in 0.05f..0.2f,
                mark.top in 0.05f..0.2f,
                elsewhere.size
            )
        )
    }

    // Code review of 2026-11-11: the last PDF opened stayed loaded after its tab closed.
    @Test
    fun `a PDF is let go when its tab closes, and another PDF's release leaves it open`() =
        runBlocking {
            DocumentFixtures.pdf(file = File(vault, path.value))

            pages.count(
                vaultPath = vault.path,
                path = path
            )

            pages.release(vault.path, RelativePath(value = "Other.pdf"))
            val afterOther = settledOpen()

            pages.release(vault.path, path)
            val afterItsOwn = settledOpen()

            assertEquals(
                expected = listOf(true, false),
                actual = listOf(afterOther, afterItsOwn)
            )
        }

    // Whether the PDF is open once the release, done in the background, has run.
    private suspend fun settledOpen(): Boolean {
        delay(timeMillis = SETTLE_MILLIS)

        return pages.isOpen
    }

    private companion object {
        const val SETTLE_MILLIS = 300L
    }
}
