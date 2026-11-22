package app.notekea.clips.presentation

import app.notekea.clips.domain.Article
import app.notekea.clips.domain.ClipSectionUseCase
import app.notekea.clips.domain.ClipWebPageUseCase
import app.notekea.clips.domain.DescribeSectionUseCase
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.FindSectionUseCase
import app.notekea.clips.domain.SupportCheck
import app.notekea.clips.domain.UpdateWebSourcesUseCase
import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeAttachmentStore
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeNoteFileOperations
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.WebSourcesFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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

    private val attachments = FakeAttachmentStore()

    private fun viewModel() = ClipViewModel(
        vaultPath = VAULT,
        clipWebPage = clip,
        findSection = FindSectionUseCase(fetcher = { url ->
            Outcome.Success(FetchedPage(url, pages[url].orEmpty()))
        }),
        clipSection = ClipSectionUseCase(clipWebPage = clip),
        describeSection = DescribeSectionUseCase(llmEngine = engine),
        updateWebSources = UpdateWebSourcesUseCase(
            clipWebPage = clip,
            findSection = FindSectionUseCase(fetcher = { url ->
                Outcome.Success(FetchedPage(url, pages[url].orEmpty()))
            }),
            attachmentStore = attachments,
            noteContentStore = store,
            noteFileOperations = files
        ),
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

    // The code review of 2026-11-22: a note the vault refused closed the dialog as if it had been written.
    @Test
    fun `a note the vault refuses is said in the dialog, which stays open, and no empty note is left`() =
        runTest(dispatcher) {
            val note = RelativePath(value = "Web sources/Introduction.md")
            val viewModel = asked()

            store.refused += note
            viewModel.onIntent(intent = ClipIntent.OnClipPage)
            advanceUntilIdle()

            val state = viewModel.state.value

            assertEquals(
                expected = listOf(true, true, false, false),
                actual = listOf(
                    state.hasWriteFailed,
                    state.isOpen,
                    state.isBusy,
                    note in files.existing
                )
            )
        }

    // The code review of 2026-11-22: a section with a page the vault refused was opened on notes left empty.
    @Test
    fun `a section with a note the vault refuses is said in the dialog instead of opened`() =
        runTest(dispatcher) {
            val viewModel = asked()

            store.refused += RelativePath(value = "Web sources/Introduction/Axiom.md")
            viewModel.onIntent(intent = ClipIntent.OnClipSection)
            advanceUntilIdle()

            val state = viewModel.state.value

            assertEquals(
                expected = listOf(true, true, false),
                actual = listOf(
                    state.hasWriteFailed,
                    state.isOpen,
                    state.isBusy
                )
            )
        }

    // The code review of 2026-11-22: an update that threw left its dialog on "Looking at the notes…" for good.
    @Test
    fun `an update that breaks is shown as failed in its dialog`() = runTest(dispatcher) {
        val viewModel = viewModel()

        attachments.contents[RelativePath(value = "Web sources/Old.md")] = ByteArray(size = 0)
        store.onRead = { error("The disk broke") }

        viewModel.onIntent(
            intent = ClipIntent.OnUpdate(
                path = WebSourcesFolder.PATH,
                isShown = true
            )
        )

        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals(
            expected = listOf<Any?>(true, true, null),
            actual = listOf(
                state.hasUpdateFailed,
                state.isUpdateShown,
                state.update
            )
        )
    }

    // The code review of 2026-11-22: the update named the notes it rewrote only, so unsaved edits undid it.
    @Test
    fun `an update tells the editors the text each rewritten note had before`() =
        runTest(dispatcher) {
            val note = RelativePath(value = "Web sources/Axiom.md")
            val viewModel = viewModel()
            val effects = mutableListOf<ClipEffect>()

            val collector = launch {
                viewModel.effects.collect { effects += it }
            }

            store.texts[note] = CLIPPED
            attachments.contents[note] = ByteArray(size = 0)
            viewModel.onIntent(intent = ClipIntent.OnUpdate(path = note))
            advanceUntilIdle()
            collector.cancel()

            assertEquals(
                expected = listOf<ClipEffect>(
                    ClipEffect.Rewritten(originals = mapOf(note to CLIPPED))
                ),
                actual = effects
            )
        }

    private companion object {
        const val VAULT = "/vault"

        const val CLIPPED = "---\nsource: \"https://x.com/docs/integrations/axiom\"\n" +
            "clipped: 2026-10-01\n---\n\n# Title\n\n## Original text\n\nOld text.\n"
        const val URL = "https://x.com/docs/integrations/introduction"
        const val PREFIX = "https://x.com/docs/integrations/"
    }
}
