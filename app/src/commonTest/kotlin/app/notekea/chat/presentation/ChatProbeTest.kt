package app.notekea.chat.presentation

import app.notekea.chat.domain.ActionProblem
import app.notekea.chat.domain.AnswerPrompt
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.CompactChatUseCase
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.FoundNotes
import app.notekea.chat.domain.FoundRow
import app.notekea.chat.domain.TurnOutcome
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
 * The chat probe of 2026-11-11 in the chat itself: a question that found tables is answered in words, a refusal says
 * its own why, and the chat's memory fills, is compacted into a summary, and gives it to the next question.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatProbeTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()
    private var plan = ""

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)

        engine.reply = { messages ->
            val system = messages.first().text

            when {
                system.startsWith("You turn a request") -> plan
                system.startsWith("Summarize this conversation") -> "They asked about arepas and books."
                system.contains("from the tables of the person's notes") -> TABLES_ANSWER
                else -> "A long answer about the notes. ".repeat(n = 20)
            }
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val clock = app
        .notekea
        .core
        .domain
        .FakeClock(
            currentTime = kotlin
                .time
                .Instant
                .parse("2026-11-11T09:00:00Z")
        )

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
    fun `a question the plan answers with tables is answered in words above them`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"find_notes\",\"request\":\"books finished\"}]}"
            runner.found = FoundNotes(rows = listOf(FoundRow(note = RelativePath(value = "Books/Dune.md"))))

            val entry = asked(
                viewModel = started(),
                question = "cuántos libros terminé?"
            )

            assertEquals(
                expected = listOf<Any?>(TABLES_ANSWER, TurnOutcome.ANSWERED, 1),
                actual = listOf(entry.answer, entry.outcome, entry.action?.found?.size)
            )
        }

    // The chat probe of 2026-11-11: the tables' answer was cut at its first words, its turn already ended.
    @Test
    fun `the turn is answering while the tables found are answered in words`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"find_notes\",\"request\":\"books finished\"}]}"
            runner.found = FoundNotes(rows = listOf(FoundRow(note = RelativePath(value = "Books/Dune.md"))))
            val viewModel = started()
            var isAnswering = false
            val tablesReply = engine.reply

            engine.reply = { messages ->
                val isTables = messages
                    .first()
                    .text
                    .contains("from the tables")

                if (isTables) isAnswering = viewModel.state.value.isAnswering
                tablesReply(messages)
            }

            asked(
                viewModel = viewModel,
                question = "how many books did I finish?"
            )

            assertEquals(
                expected = true,
                actual = isAnswering
            )
        }

    @Test
    fun `a refusal is the model's own why, in the person's words`() = runTest(dispatcher) {
        plan = "{\"steps\":[{\"action\":\"refuse\",\"text\":\"Notekea no puede enviar recordatorios.\"}]}"

        val entry = asked(
            viewModel = started(),
            question = "mandame un recordatorio mañana a las 9"
        )

        assertEquals(
            expected = ActionProblem.Refused(reason = "Notekea no puede enviar recordatorios."),
            actual = entry.action?.problem
        )
    }

    @Test
    fun `the memory fills, compacts itself into a summary when full, and gives it to the next question`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"chat\"}]}"
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
    fun `compact chat writes the summary when asked, and clear forgets it`() = runTest(dispatcher) {
        plan = "{\"steps\":[{\"action\":\"chat\"}]}"
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

    @Test
    fun `compacting writes the earlier chat short, in the person's language`() =
        runTest(dispatcher) {
            val written = CompactChatUseCase(llmEngine = engine)(
                exchanges = listOf(
                    ChatExchange(
                        question = "¿Cuánta agua llevan las arepas?",
                        reply = "Dos tazas y media."
                    )
                ),
                summary = null
            )

            assertEquals(
                expected = true,
                actual = engine.generated.last().last().text.contains("Person: ¿Cuánta agua llevan las arepas?") &&
                    written is app.notekea.core.domain.Outcome.Success
            )
        }

    // The developer's goal of 2026-11-11: the chat compacts itself after a while, as a new session.
    @Test
    fun `a chat left for half an hour is compacted before the next question, one asked at once is not`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"chat\"}]}"
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
    fun `the chat's first exchange is given to a question long after it`() = runTest(dispatcher) {
        plan = "{\"steps\":[{\"action\":\"chat\"}]}"
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

    // The chat probe of 2026-11-11: in Auto, questions the passages could not answer were left unanswered.
    @Test
    fun `in auto a question the notes' passages do not answer is worked on by the agent`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"answer\",\"request\":\"who owns the launch?\"}]}"

            engine.readiness = app
                .notekea
                .core
                .domain
                .Outcome
                .Success(
                    value = app
                        .notekea
                        .core
                        .llm
                        .domain
                        .LlmModelInfo(
                            isSmall = false,
                            hasAgent = true
                        )
                )

            val agentReply = engine.reply

            engine.reply = { messages ->
                when {
                    messages.first().text.startsWith("You work on a task") -> "{\"move\":\"finish\",\"summary\":\"Ana.\"}"
                    else -> agentReply(messages)
                }
            }

            val entry = asked(
                viewModel = started(),
                question = "who owns the launch?"
            )

            assertEquals(
                expected = listOf<Any?>(ChatMode.AGENT, "Ana.", TurnOutcome.ANSWERED),
                actual = listOf(entry.answeredWith, entry.answer, entry.outcome)
            )
        }

    // The chat probe of 2026-11-11: a question Auto thought through was left unanswered, the agent never asked.
    @Test
    fun `in auto a deep question the passages do not answer is worked on by the agent too`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"answer\",\"request\":\"what did I learn from my books?\"}]}"

            engine.readiness = app
                .notekea
                .core
                .domain
                .Outcome
                .Success(
                    value = app
                        .notekea
                        .core
                        .llm
                        .domain
                        .LlmModelInfo(
                            isSmall = false,
                            hasAgent = true
                        )
                )

            val agentReply = engine.reply

            engine.reply = { messages ->
                when {
                    messages.first().text.startsWith("You work on a task") -> FINISHED
                    messages.first().text.startsWith("Answer in one") -> AnswerPrompt.NOT_FOUND
                    else -> agentReply(messages)
                }
            }

            val entry = asked(
                viewModel = started(),
                question = "why did I learn so little from my books, and how do I compare them?"
            )

            assertEquals(
                expected = listOf<Any?>(ChatMode.AGENT, "Focus.", TurnOutcome.ANSWERED),
                actual = listOf(entry.answeredWith, entry.answer, entry.outcome)
            )
        }

    private companion object {
        const val TABLES_ANSWER = "Terminaste 3: Deep Work, Dune y Sapiens."
        const val FINISHED = "{\"move\":\"finish\",\"summary\":\"Focus.\"}"
    }
}
