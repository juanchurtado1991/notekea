package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.plan.PageProgress
import app.notekea.chat.domain.plan.SectionAsked
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
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

/** A pasted documentation page in the chat: it asks, and the button clips the page with its links on one card. */
@OptIn(ExperimentalCoroutinesApi::class)
class ClipSectionChatFlowTest {
    private val dispatcher = StandardTestDispatcher()

    private val engine = FakeLlmEngine().apply {
        reply = { messages ->
            val request = messages
                .last()
                .text
                .substringAfterLast(delimiter = "Request: ")

            if (messages.first().text.startsWith("You turn a request")) {
                "{\"steps\":[{\"action\":\"clip_url\",\"url\":\"$URL\"}]}".takeIf {
                    "x.com" in request
                } ?: ""
            } else {
                ""
            }
        }
    }

    private val depths = mutableListOf<Int>()
    private var progressShown: () -> Unit = {}

    private val clipper = object : WebPageClipper {
        override suspend fun clip(url: String) = Outcome.Success(value = draft(name = "Overview"))

        override suspend fun sectionOf(
            url: String,
            language: String?
        ) = Outcome.Success(
            value = SectionAsked(
                url = url,
                prefix = "https://x.com/docs/",
                count = 2,
                more = 0
            )
        )

        override suspend fun clipSection(
            url: String,
            isEvery: Boolean,
            depth: Int,
            onProgress: suspend (PageProgress) -> Unit
        ): Outcome<List<NoteDraft>, PageProblem> {
            depths += depth
            onProgress(PageProgress(found = FOUND))
            progressShown()

            onProgress(
                PageProgress(
                    done = 1,
                    total = FOUND,
                    url = "https://x.com/docs/axiom"
                )
            )

            return clips()
        }

        private fun clips() = Outcome.Success(
            value = listOf(draft(name = "Overview"), draft(name = "Overview/Axiom"), draft(name = "Overview/Datadog"))
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a pasted page is asked about, and This page and 2 more proposes them all`() =
        runTest(dispatcher) {
            val viewModel = chatViewModel(
                engine = engine,
                store = FakeNoteContentStore(),
                index = FakeVectorIndex(),
                embedder = FakeTextEmbedder(),
                runner = FakeActionRunner(),
                history = FakeChatHistoryStore(),
                clipboard = FakeTextClipboard(),
                clipper = clipper
            )

            viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = listOf(entry(path = "Home.md"))))
            viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
            advanceUntilIdle()
            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "clip $URL"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            val asked = viewModel
                .state
                .value
                .entries
                .last()

            viewModel.onIntent(
                intent = ChatIntent.OnSectionChoose(
                    id = asked.id,
                    isWhole = true
                )
            )

            advanceUntilIdle()

            val chosen = viewModel
                .state
                .value
                .entries
                .last()

            assertEquals(
                // No action card on the asking turn: it was drawn in place of the question, which never showed.
                expected = listOf<Any?>(2, null, "clip $URL with its linked pages", 3),
                actual = listOf(
                    asked.sectionAsked?.count,
                    asked.action,
                    chosen.question,
                    chosen
                        .action
                        ?.steps
                        ?.single()
                        ?.content
                        ?.size
                )
            )
        }

    private fun draft(name: String) = NoteDraft(
        path = RelativePath(value = "Web sources/$name.md"),
        text = name
    )

    // The developer's request of 2026-11-13: a documentation takes minutes, and the chat said nothing meanwhile.
    @Test
    fun `two levels deep is asked as a request, and the chat shows how many pages while it clips`() =
        runTest(dispatcher) {
            val viewModel = chatViewModel(
                engine = engine,
                store = FakeNoteContentStore(),
                index = FakeVectorIndex(),
                embedder = FakeTextEmbedder(),
                runner = FakeActionRunner(),
                history = FakeChatHistoryStore(),
                clipboard = FakeTextClipboard(),
                clipper = clipper
            )

            var shown: PageProgress? = null

            progressShown = {
                shown = viewModel
                    .state
                    .value
                    .entries
                    .last()
                    .progress
            }

            viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = listOf(entry(path = "Home.md"))))
            viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
            advanceUntilIdle()
            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "clip $URL"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            viewModel.onIntent(
                intent = ChatIntent.OnSectionChoose(
                    id = viewModel
                        .state
                        .value
                        .entries
                        .last()
                        .id,
                    isWhole = true,
                    depth = 2
                )
            )

            advanceUntilIdle()

            val chosen = viewModel
                .state
                .value
                .entries
                .last()

            assertEquals(
                expected = listOf<Any?>(
                    "clip $URL with its linked pages, 2 levels deep",
                    listOf(2),
                    PageProgress(found = FOUND),
                    3
                ),
                actual = listOf(
                    chosen.question,
                    depths,
                    shown,
                    chosen
                        .action
                        ?.steps
                        ?.single()
                        ?.content
                        ?.size
                )
            )
        }

    private companion object {
        const val URL = "https://x.com/docs/overview"
        const val FOUND = 12
    }
}
