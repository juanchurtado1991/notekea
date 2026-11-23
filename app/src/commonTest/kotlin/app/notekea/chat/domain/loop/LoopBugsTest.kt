package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * Bugs of the loop's code, not of the model (the bug hunt of 2026-11-17, each seen in the eighth run): a change taken
 * out could never be added again, two refused changes took turns until step 30, and a figure the task writes settled
 * which note it meant.
 */
class LoopBugsTest {
    private val engine = FakeLlmEngine()

    private val cache = NoteTextCache(
        contentStore = FakeNoteContentStore()
    )

    private val notes = listOf("Recipes/Arepas.md", "Recipes/Black beans.md").map {
        entry(path = it)
    }

    private val runTurn = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ORGANIZE to listOf(
                    ChangeTools.addTag,
                    ChangeTools.addTask,
                    ProposeTool(),
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
        clock = Clock.System
    )

    private suspend fun steps(
        task: String,
        calls: (Int) -> String
    ): List<AgentStep> {
        var made = 0

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"organize","skill":"none"}"""
                asked.startsWith(prefix = CALLS) -> calls(made++)
                else -> "Nothing was changed."
            }
        }

        return runTurn(
            task = task,
            vaultPath = VAULT,
            notes = notes,
            context = ActionContext(
                today = "2026-11-17",
                notes = notes.map { it.file.path.nameWithoutExtension }
            ),
            openNote = null
        ).toList()
    }

    @Test
    fun `a change taken out with drop is added again when it is asked for again`() = runTest {
        val tag = """{"tool":"add_tag","notes":"Recipes/Arepas","tag":"latin"}"""
        val drop = """{"tool":"propose","summary":"Tag.","drop":"Recipes/Arepas"}"""
        val propose = """{"tool":"propose","summary":"Tag Arepas with latin."}"""
        val script = listOf(tag, drop, tag, propose)

        val last = steps(task = "tag the arepas with latin") {
            script[it.coerceAtMost(script.lastIndex)]
        }.last()

        assertEquals(
            expected = listOf("Arepas"),
            actual = (last as AgentStep.Proposed)
                .steps
                .flatMap { it.notes }
                .map { it.nameWithoutExtension }
        )
    }

    @Test
    fun `two changes refused over and over end the turn long before its last step`() = runTest {
        val task = """{"tool":"add_task","note":"Recipes/Arepas","text":"Tag it"}"""
        val untagged = """{"tool":"add_tag","notes":"Recipes/Nowhere","tag":"latin"}"""
        val answer = """{"tool":"answer","text":"Nothing was changed."}"""

        val calls = steps(task = "tag the arepas with latin") { made ->
            if (made > MOST_CALLS) answer else listOf(task, untagged)[made % 2]
        }

        assertEquals(
            expected = true,
            actual = calls.count {
                it is AgentStep.Moving
            } <= MOST_CALLS
        )
    }

    // The code review of 2026-11-22: a change put in an earlier one's place left the proposal's size as it was, so
    // it was taken as refused and its tool was not offered next.
    @Test
    fun `a change that replaces an earlier one is not refused, and its tool is offered next`() =
        runTest {
            val one = """{"tool":"add_tag","notes":["Recipes/Arepas"],"tag":"latin"}"""

            val both =
                """{"tool":"add_tag","notes":["Recipes/Arepas","Recipes/Black beans"],"tag":"latin"}"""

            val propose = """{"tool":"propose","summary":"Tag both with latin."}"""
            val script = listOf(one, both, propose)

            steps(task = "tag the arepas and the black beans with latin") {
                script[it.coerceAtMost(script.lastIndex)]
            }

            val offered = engine
                .generated
                .indices
                .filter {
                    engine
                        .generated[it]
                        .first()
                        .text
                        .startsWith(prefix = CALLS)
                }
                .map {
                    engine
                        .options[it]
                        .jsonSchema
                        .orEmpty()
                }

            assertEquals(
                expected = true,
                actual = "add_tag" in offered[2]
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val MOST_CALLS = 12
    }
}
