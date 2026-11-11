package app.notekea.clips.presentation

import app.notekea.clips.domain.Article
import app.notekea.clips.domain.ClipSectionUseCase
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.DescribeSectionUseCase
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.FindSectionUseCase
import app.notekea.clips.domain.SupportCheck
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * "Clip a web page" on a documentation page (the developer's request of 2026-11-11): it asks whether to clip only the
 * page or the pages it links under its path too, and writes one note, or the page as the index of a folder of notes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ClipSectionDialogTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine().apply {
        readiness = Outcome.Failure(error = LlmError.NoModel)
    }
    private val store = FakeNoteContentStore()
    private val files = FakeNoteFileOperations()

    private val pages = mapOf(
        URL to "<a href=\"/docs/integrations/axiom\">Axiom</a><a href=\"/docs/integrations/datadog\">Datadog</a>",
        "https://x.com/docs/integrations/axiom" to "",
        "https://x.com/docs/integrations/datadog" to ""
    )

    private val clip = ClipWebPageUseCase(
        fetcher = { url ->
            Outcome.Success(
                value = FetchedPage(
                    url = url,
                    html = pages[url].orEmpty()
                )
            )
        },
        extractor = { page ->
            Outcome.Success(
                value = Article(
                    title = page
                        .url
                        .substringAfterLast(delimiter = '/')
                        .replaceFirstChar { it.uppercase() },
                    paragraphs = listOf("About ${page.url}.")
                )
            )
        },
        llmEngine = engine,
        supportCheck = SupportCheck(llmEngine = engine)
    )

    private fun viewModel() = ClipViewModel(
        vaultPath = VAULT,
        clipWebPage = clip,
        findSection = FindSectionUseCase(fetcher = { url ->
            Outcome.Success(FetchedPage(url, pages[url].orEmpty()))
        }),
        clipSection = ClipSectionUseCase(clipWebPage = clip),
        describeSection = DescribeSectionUseCase(llmEngine = engine),
        noteContentStore = store,
        noteFileOperations = files,
        localCalendar = FakeLocalCalendar(),
        clock = FakeClock(currentTime = Instant.parse("2026-11-11T09:00:00Z"))
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun kotlinx.coroutines.test.TestScope.asked(): ClipViewModel {
        val viewModel = viewModel()

        viewModel.onIntent(intent = ClipIntent.OnOpen)
        viewModel.onIntent(intent = ClipIntent.OnUrlChange(url = URL))
        viewModel.onIntent(intent = ClipIntent.OnClip)
        advanceUntilIdle()

        return viewModel
    }

    @Test
    fun `a page that links pages under its path asks first, and writes nothing until answered`() =
        runTest(dispatcher) {
            val state = asked().state.value

            assertEquals(
                expected = listOf<Any?>(true, 2, PREFIX, emptyMap<Any, Any>()),
                actual = listOf(
                    state.isAsking,
                    state.section?.urls?.size,
                    state.section?.prefix,
                    store.texts
                )
            )
        }

    @Test
    fun `only this page writes one note, the page and its links write a folder with the page as its index`() =
        runTest(dispatcher) {
            asked().onIntent(intent = ClipIntent.OnClipPage)
            advanceUntilIdle()

            val alone = store
                .texts
                .keys
                .map { it.value }

            store.texts.clear()
            files.existing.clear()
            asked().onIntent(intent = ClipIntent.OnClipSection)
            advanceUntilIdle()

            assertEquals(
                expected = listOf(
                    listOf("Web sources/Introduction.md"),
                    listOf(
                        "Web sources/Introduction.md",
                        "Web sources/Introduction/Axiom.md",
                        "Web sources/Introduction/Datadog.md"
                    )
                ),
                actual = listOf(
                    alone,
                    store
                        .texts
                        .keys
                        .map { it.value }
                )
            )
        }

    private companion object {
        const val VAULT = "/vault"
        const val URL = "https://x.com/docs/integrations/introduction"
        const val PREFIX = "https://x.com/docs/integrations/"
    }
}
