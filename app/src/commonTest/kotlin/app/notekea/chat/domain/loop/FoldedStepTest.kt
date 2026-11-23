package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.context.FakeStepMemory
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
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
 * The step's prompt shown less on purpose (`docs/agent-step-memory.md`): with a memory, a long result before the
 * last three is folded to its first line, and a read made again gets it back whole, not "its result is above".
 */
class FoldedStepTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder
    )

    private val lease = RelativePath(value = "Home/Lease.md")

    private val runTurn = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(
                    ReadTool(noteTextCache = cache),
                    CalculateTool(),
                    AnswerTool()
                )
            )
        ),
        vaultGuide = VaultGuide(noteTextCache = cache),
        proposal = ProposalCheck(
            checkPlan = CheckPlanUseCase(),
            composeNotes = ComposeNotesUseCase(
                llmEngine = engine,
                noteTextCache = cache,
                planReader = GhostActionPlanReader()
            )
        ),
        clock = Clock.System,
        stepMemory = FakeStepMemory(),
        wholeLast = StepRecall.WHOLE_LAST
    )

    @Test
    fun `a long old result is folded, and read again it is shown whole`() = runTest {
        val rent = "The rent is 950 euros a month. ".repeat(n = 30)
        store.texts[lease] = "# Lease\n\n## Rent\n$rent"
        script()
        run()

        val prompts = engine
            .generated
            .filter {
                it
                    .first()
                    .text
                    .startsWith(prefix = CALLS)
            }
            .map { it.last().text }

        assertEquals(
            expected = listOf(false, true, true),
            actual = listOf(
                FOLDED in prompts[3],
                FOLDED in prompts[4],
                "its result was kept aside, so here it is whole" in prompts[5]
            )
        )
    }

    private fun script() {
        val calls = ArrayDeque(
            elements = listOf(
                """{"tool":"read","note":"Lease"}""",
                """{"tool":"calculate","expression":"1 + 1"}""",
                """{"tool":"calculate","expression":"2 + 2"}""",
                """{"tool":"calculate","expression":"3 + 3"}""",
                """{"tool":"read","note":"Lease"}""",
                """{"tool":"answer","text":"950 euros a month."}"""
            )
        )

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> ROUTE
                asked.startsWith(prefix = CALLS) -> calls.removeFirst()
                else -> "The rent is **11,400 euros** a year."
            }
        }
    }

    private suspend fun run() = runTurn(
        task = "how much is the rent a year?",
        vaultPath = VAULT,
        notes = listOf(entry(path = lease.value)),
        context = ActionContext(
            today = "2026-11-16",
            notes = listOf("Home/Lease")
        ),
        openNote = null
    ).toList()

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ROUTE = """{"work":"ask","skill":"none"}"""
        const val FOLDED = "kept aside: recall it"
    }
}
