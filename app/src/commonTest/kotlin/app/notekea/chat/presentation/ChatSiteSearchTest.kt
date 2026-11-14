package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.plan.PageProgress
import app.notekea.chat.domain.sources.ClippedPage
import app.notekea.chat.domain.sources.SourcePage
import app.notekea.chat.domain.sources.SourceSites
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * "I do not have it, shall I look for it?" (the developer's request of 2026-11-13): a question the notes do not
 * answer is offered the site of the vault's web sources, its pages are listed, and only the ticked ones are clipped.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatSiteSearchTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine()
    private val clipped = mutableListOf<String>()

    private val pages = listOf(
        SourcePage(
            url = "https://x.org/docs/store",
            about = "Set up a store",
            section = "Web sources/Docs.md"
        ),
        SourcePage(
            url = "https://x.org/docs/payments",
            about = "Take payments",
            section = "Web sources/Docs.md"
        )
    )

    private val sites = object : SourceSites {
        override suspend fun sitesOf(vaultPath: String) = listOf("x.org")

        override suspend fun pagesFor(
            vaultPath: String,
            question: String,
            onProgress: suspend (PageProgress) -> Unit
        ) = pages

        override suspend fun clip(
            vaultPath: String,
            pages: List<SourcePage>,
            onProgress: suspend (PageProgress) -> Unit
        ): List<ClippedPage> {
            clipped += pages.map { it.url }

            return pages.map {
                ClippedPage(
                    path = RelativePath(value = "Web sources/Docs/Store.md"),
                    text = "A store needs the shop plugin."
                )
            }
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith("You turn a request") -> "{\"steps\":[{\"action\":\"answer\"}]}"
                asked.startsWith("Write the answer to the task") -> "Install the **shop plugin**."
                else -> "NOT_FOUND"
            }
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun last(viewModel: ChatViewModel): ChatEntry = viewModel
        .state
        .value
        .entries
        .last()

    private fun TestScope.send(
        viewModel: ChatViewModel,
        intent: ChatIntent
    ) {
        viewModel.onIntent(intent = intent)
        advanceUntilIdle()
    }

    @Test
    fun `not found offers the site, lists its pages ticked, and clips only the ones left ticked to answer`() =
        runTest(dispatcher) {
            val viewModel = chatViewModel(
                engine = engine,
                store = FakeNoteContentStore(),
                index = FakeVectorIndex(),
                embedder = FakeTextEmbedder(),
                runner = FakeActionRunner(),
                history = FakeChatHistoryStore(),
                clipboard = FakeTextClipboard(),
                sourceSites = sites
            )

            send(viewModel, ChatIntent.OnIndexChange(isReady = true))
            send(viewModel, ChatIntent.OnQuestionChange(text = "how do I open a store?"))
            send(viewModel, ChatIntent.OnSend)
            val offered = last(viewModel)
            val id = offered.id

            send(viewModel, ChatIntent.OnSiteSearch(id = id))
            val listed = last(viewModel)

            send(
                viewModel,
                ChatIntent.OnSitePageToggle(
                    id = id,
                    url = pages.last().url
                )
            )

            val nothingYet = clipped.toList()
            send(viewModel, ChatIntent.OnSiteClip(id = id))
            val answered = last(viewModel)

            assertEquals(
                expected = listOf<Any?>(
                    TurnOutcome.NOT_FOUND to listOf("x.org"),
                    pages to pages.map { it.url }.toSet(),
                    emptyList<String>(),
                    listOf(pages.first().url),
                    TurnOutcome.ANSWERED to "Install the **shop plugin**."
                ),
                actual = listOf(
                    offered.outcome to offered.sites,
                    listed.sitePages to listed.pickedPages,
                    nothingYet,
                    clipped,
                    answered.outcome to answered.answer
                )
            )
        }
}
