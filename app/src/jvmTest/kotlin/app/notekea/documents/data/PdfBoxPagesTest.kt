package app.notekea.documents.data

import app.notekea.core.data.JvmAppDispatchers
import app.notekea.core.documents.data.DocumentFixtures
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
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
}
