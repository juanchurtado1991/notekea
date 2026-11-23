package app.notekea.chat.domain.agent

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * No prompt of the agent is longer than the model's context (`docs/agent-loop.md`, step 2): what it found is
 * fitted in tokens before every move, where 9,000 characters were kept whatever they weighed.
 */
class AgentContextTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder
    )

    private val agent = AgentTaskUseCase(
        llmEngine = engine,
        planReader = GhostActionPlanReader(),
        answerFromNotes = AnswerFromNotesUseCase(
            textEmbedder = FakeTextEmbedder(),
            vectorIndex = FakeVectorIndex(),
            noteTextCache = cache,
            llmEngine = engine
        ),
        noteTextCache = cache,
        actionRunner = FakeActionRunner(),
        pageReader = { Outcome.Success(value = "") },
        checkPlan = CheckPlanUseCase(),
        composeNotes = ComposeNotesUseCase(
            llmEngine = engine,
            noteTextCache = cache,
            planReader = GhostActionPlanReader()
        ),
        clock = Clock.System
    )

    @Test
    fun `twelve long notes read one after another never make a prompt longer than the context`() =
        runTest {
            val paths = (1..NOTES).map { RelativePath(value = "Folder/Note $it.md") }
            paths.forEach { store.texts[it] = "A fact about the lease. ".repeat(n = 120) }

            val moves = ArrayDeque(
                elements = paths.map { "{\"move\":\"read\",\"note\":\"${it.nameWithoutExtension}\"}" } +
                    "{\"move\":\"finish\",\"summary\":\"Done.\"}"
            )

            engine.reply = { messages ->
                val isMove = messages
                    .first()
                    .text
                    .startsWith(prefix = "You work on a task")

                if (isMove) moves.removeFirst() else "Done."
            }

            agent(
                task = "read every note of the folder and say what they agree on",
                vaultPath = VAULT,
                notes = paths.map { entry(path = it.value) },
                context = ActionContext(
                    today = "2026-11-14",
                    notes = paths.map { it.bare() }
                ),
                openNote = null
            ).toList()

            val prompts = engine.generated.filter {
                it
                    .first()
                    .text
                    .startsWith(prefix = "You work on a task")
            }

            val longest = prompts.maxOf { messages ->
                messages.sumOf { engine.countTokens(text = it.text) }
            }

            assertEquals(
                expected = listOf(NOTES + 1, true, true),
                actual = listOf(
                    prompts.size,
                    longest + REPLY_TOKENS <= CONTEXT_TOKENS,
                    ContextBudget.droppedLine(count = 1).substringAfter(delimiter = "earliest") in
                        prompts
                            .last()
                            .last()
                            .text
                )
            )
        }

    private companion object {
        const val NOTES = 12
        const val REPLY_TOKENS = 1_600
        const val CONTEXT_TOKENS = 4_096
    }
}
