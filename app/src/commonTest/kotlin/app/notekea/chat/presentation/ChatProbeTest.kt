package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.chat.presentation.viewmodel.after
import app.notekea.core.domain.FakeClock
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * The chat probe of 2026-11-11 in the chat itself: the chat's memory fills, is compacted into a summary, and gives it
 * to the next question, and the talk knows what changed today.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatProbeTest : ViewModelTestBase() {
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()

    @BeforeTest
    fun setUp() {
        engine.reply = { messages ->
            val system = messages.first().text

            when {
                system.startsWith("You say how a request") -> TALK
                system.startsWith("Summarize this conversation") -> "They asked about arepas and books."
                else -> "A long answer about the notes. ".repeat(n = 20)
            }
        }
    }

    private val clock = FakeClock(currentTime = Instant.parse("2026-11-11T09:00:00Z"))

    private fun TestScope.started(): ChatViewModel {
        val viewModel = chatViewModel(
            clock = clock,
            engine = engine,
            store = FakeNoteContentStore(),
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = runner,
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard()
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = listOf(entry(path = "Books/Dune.md"))))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        return viewModel
    }

    private fun TestScope.asked(
        viewModel: ChatViewModel,
        question: String
    ): ChatEntry {
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = question))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()

        return viewModel
            .state
            .value
            .entries
            .last()
    }

    @Test
    fun `the memory fills, compacts itself into a summary when full, and gives it to the next question`() =
        runTest(testDispatcher) {
            val viewModel = started()

            val usages = (1..16).map {
                asked(
                    viewModel = viewModel,
                    question = "tell me more about the notes, part $it"
                )

                viewModel.state.value.contextUsage
            }

            asked(
                viewModel = viewModel,
                question = "and the last one?"
            )

            val talk = engine.generated.last {
                it
                    .first()
                    .text
                    .startsWith("You are")
            }

            val summary = talk.any { it.text.contains("They asked about arepas and books.") }

            assertEquals(
                expected = listOf(true, true, true),
                actual = listOf(
                    usages.first() < usages[5],
                    usages.max() < 1f || usages.last() < usages.max(),
                    summary
                )
            )
        }

    @Test
    fun `compact chat writes the summary when asked, and clear forgets it`() =
        runTest(testDispatcher) {
            val viewModel = started()

            repeat(times = 4) {
                asked(
                    viewModel = viewModel,
                    question = "question $it"
                )
            }

            val before = viewModel.state.value.contextUsage
            viewModel.onIntent(intent = ChatIntent.OnCompact)
            advanceUntilIdle()
            val after = viewModel.state.value.contextUsage
            viewModel.onIntent(intent = ChatIntent.OnClearHistory)
            advanceUntilIdle()

            assertEquals(
                expected = listOf(true, 0f),
                actual = listOf(after < before, viewModel.state.value.contextUsage)
            )
        }

    // The developer's goal of 2026-11-11: the chat compacts itself after a while, as a new session.
    @Test
    fun `a chat left for half an hour is compacted before the next question, one asked at once is not`() =
        runTest(testDispatcher) {
            val viewModel = started()

            repeat(times = 4) {
                asked(
                    viewModel = viewModel,
                    question = "question $it"
                )
            }

            val summaries = {
                engine.generated.count {
                    it
                        .first()
                        .text
                        .startsWith("Summarize this conversation")
                }
            }

            val before = summaries()

            clock.currentTime += kotlin
                .time
                .Duration
                .parse("31m")

            asked(
                viewModel = viewModel,
                question = "back again"
            )

            val afterIdle = summaries()

            asked(
                viewModel = viewModel,
                question = "and now?"
            )

            assertEquals(
                expected = listOf(before + 1, before + 1),
                actual = listOf(afterIdle, summaries())
            )
        }

    // The chat probe of 2026-11-11: "going back to the start: how much flour was it?" after ten turns had lost it.
    @Test
    fun `the chat's first exchange is given to a question long after it`() =
        runTest(testDispatcher) {
            val viewModel = started()

            listOf("how much flour do the arepas take?", "a", "b", "c", "d", "e").forEach {
                asked(
                    viewModel = viewModel,
                    question = it
                )
            }

            asked(
                viewModel = viewModel,
                question = "going back to the start: how much flour was it?"
            )

            val talk = engine.generated.last {
                it
                    .first()
                    .text
                    .startsWith("You are")
            }

            assertEquals(
                expected = true,
                actual = talk.any {
                    it.text == "how much flour do the arepas take?"
                }
            )
        }

    // The Auto-Test on the real app, 2026-11-11: with a long chat from an earlier session, the ring stayed on
    // Compacting after the replies.
    @Test
    fun `a long chat from an earlier session is compacted, and the ring says the memory again`() =
        runTest(testDispatcher) {
            val history = FakeChatHistoryStore()

            history.turns[app.notekea.core.notes.domain.NoteFixtures.VAULT] = (1..60).map {
                app
                    .notekea
                    .chat
                    .domain
                    .ChatTurn(
                        question = "earlier question $it",
                        scope = app.notekea.chat.domain.ChatScope.VAULT,
                        outcome = TurnOutcome.ANSWERED,
                        answer = "A long answer about the notes. ".repeat(n = 20),
                        sources = emptyList(),
                        scopeName = null
                    )
            }

            val viewModel = chatViewModel(
                engine = engine,
                store = FakeNoteContentStore(),
                index = FakeVectorIndex(),
                embedder = FakeTextEmbedder(),
                runner = runner,
                history = history,
                clipboard = FakeTextClipboard(),
                clock = clock
            )

            viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
            advanceUntilIdle()

            repeat(times = 3) {
                asked(
                    viewModel = viewModel,
                    question = "question $it"
                )
            }

            val state = viewModel.state.value

            assertEquals(
                expected = listOf(false, true),
                actual = listOf(state.isCompacting, state.contextUsage < 1f)
            )
        }

    // The comparison with Claude, 2026-11-11: "What did I change today?" was answered "I cannot access your computer".
    @Test
    fun `the talk knows the notes changed today and the last one changed`() =
        runTest(testDispatcher) {
            clock.currentTime = kotlin
                .time
                .Instant
                .fromEpochMilliseconds(epochMilliseconds = 1_800_000_000_000L)

            val viewModel = started()

            viewModel.onIntent(
                intent = ChatIntent.OnNotesChange(
                    notes = listOf(
                        entry(path = "Books/Dune.md"),
                        entry(
                            path = "Recipes/Old.md",
                            modifiedOffsetMillis = -THREE_DAYS
                        )
                    )
                )
            )

            asked(
                viewModel = viewModel,
                question = "What did I change today?"
            )

            val talk = engine
                .generated
                .last()
                .first()
                .text

            assertEquals(
                expected = true,
                actual = "Notes changed today: Books/Dune. The last changed note is Books/Dune" in
                    talk
            )
        }

    private companion object {
        const val THREE_DAYS = 3L * 24 * 60 * 60 * 1_000
        const val TALK = """{"work":"talk","skill":"none"}"""
    }
}
