package app.notekea.documents.presentation

import app.notekea.core.documents.domain.DocumentError
import app.notekea.core.documents.domain.DocumentReader
import app.notekea.core.documents.domain.DocumentText
import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.FakeFileOpener
import app.notekea.core.notes.domain.RelativePath
import app.notekea.documents.domain.PageMark
import app.notekea.documents.domain.PdfPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.io.encoding.Base64
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A PDF's tab (4.5): its pages are drawn as they come into view, and let go with the open PDF once the tab leaves the
 * screen, since its ViewModel stays while the vault is open (memory leak hunt, 2026-11-11).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewerViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val pages = CountingPdfPages()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(reader: DocumentReader = TextReader()) = DocumentViewerViewModel(
        vaultPath = VAULT,
        path = GUIDE,
        reader = reader,
        pdfPages = pages,
        fileOpener = FakeFileOpener(),
        dispatchers = FakeAppDispatchers(dispatcher = dispatcher)
    )

    // The code review of 2026-11-22: a page whose drawing threw was taken as still being drawn, for good.
    @Test
    fun `a page whose drawing broke is drawn the next time it comes into view`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            pages.brokenRenders = 1
            advanceUntilIdle()
            viewModel.onIntent(intent = DocumentViewerIntent.OnPageShown(page = 0))
            advanceUntilIdle()
            val broken = viewModel.state.value.pages.keys
            viewModel.onIntent(intent = DocumentViewerIntent.OnPageShown(page = 0))
            advanceUntilIdle()

            assertEquals(
                expected = listOf(emptySet<Int>(), setOf(0)),
                actual = listOf(broken, viewModel.state.value.pages.keys)
            )
        }

    // The code review of 2026-11-22: a document whose reading threw left its tab on the spinner for good.
    @Test
    fun `a document whose reading breaks stops loading and says why`() = runTest(dispatcher) {
        val viewModel = viewModel(reader = BrokenReader())

        advanceUntilIdle()

        assertEquals(
            expected = listOf<Any?>(false, DocumentError.Unreadable(message = BROKEN)),
            actual = listOf(viewModel.state.value.isLoading, viewModel.state.value.error)
        )
    }

    @Test
    fun `a page in view is drawn, and a hidden tab lets its pages and its PDF go`() =
        runTest(dispatcher) {
            val viewModel = viewModel()

            advanceUntilIdle()
            viewModel.onIntent(intent = DocumentViewerIntent.OnPageShown(page = 0))
            advanceUntilIdle()
            val drawn = viewModel.state.value.pages.keys
            viewModel.onIntent(intent = DocumentViewerIntent.OnHidden)
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any>(setOf(0), emptySet<Int>(), 1),
                actual = listOf(drawn, viewModel.state.value.pages.keys, pages.releases)
            )
        }

    private class TextReader : DocumentReader {
        override suspend fun read(
            vaultPath: String,
            path: RelativePath
        ): Outcome<DocumentText, DocumentError> =
            Outcome.Success(value = DocumentText(text = "## Page 1\n\nTrams.\n"))
    }

    private class BrokenReader : DocumentReader {
        override suspend fun read(
            vaultPath: String,
            path: RelativePath
        ): Outcome<DocumentText, DocumentError> = error(BROKEN)
    }

    private class CountingPdfPages : PdfPages {
        var releases = 0
        var brokenRenders = 0

        override suspend fun count(
            vaultPath: String,
            path: RelativePath
        ): Outcome<Int, DocumentError> = Outcome.Success(value = 3)

        override suspend fun render(
            vaultPath: String,
            path: RelativePath,
            page: Int,
            scale: Float
        ): Outcome<ByteArray, DocumentError> {
            if (brokenRenders-- > 0) error(BROKEN)

            return Outcome.Success(value = Base64.decode(source = PNG))
        }

        override suspend fun marks(
            vaultPath: String,
            path: RelativePath,
            page: Int,
            passage: String
        ): List<PageMark> = emptyList()

        override fun release(
            vaultPath: String,
            path: RelativePath
        ) {
            releases += 1
        }
    }

    private companion object {
        const val VAULT = "/home/ana/Notes"
        const val BROKEN = "The PDF broke"
        const val PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
        val GUIDE = RelativePath(value = "Guide.pdf")
    }
}
