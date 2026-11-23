package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * A question whose answers are one same note, by its name and by its path (seen on the 4B, 2026-11-16): held back,
 * the model told they are the same note, and the turn goes on; two notes that share a name are still asked.
 */
class SameNoteQuestionTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder
    )

    private val runTurn = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(
                    ReadTool(noteTextCache = cache),
                    AskUserTool(),
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

    // The model asks which of the options, until it is told they are one note; then it answers.
    private fun script(options: String) {
        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""

                asked.startsWith(prefix = CALLS) && SAME in messages.last().text ->
                    """{"tool":"answer","text":"Ship on 1 December."}"""

                asked.startsWith(prefix = CALLS) ->
                    """{"tool":"ask_user","question":"Which kickoff note?","options":"$options"}"""

                else -> "Ship on 1 December."
            }
        }
    }

    private suspend fun run(paths: List<String>): List<AgentStep> = runTurn(
        task = "what did we decide at the kickoff?",
        vaultPath = VAULT,
        notes = paths.map { entry(path = it) },
        context = ActionContext(
            today = "2026-11-16",
            notes = paths.map { it.removeSuffix(suffix = ".md") }
        ),
        openNote = null
    ).toList()

    @Test
    fun `a question whose answers are one note is held back and the turn answers`() = runTest {
        script(options = "2026-10-21 Kickoff\\nMeetings/2026-10-21 Kickoff")

        val steps = run(paths = listOf("Meetings/2026-10-21 Kickoff.md", "Projects/Launch.md"))

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                steps.last() is AgentStep.Finished,
                engine.generated.any { call ->
                    call.any {
                        SAME in it.text
                    }
                }
            )
        )
    }

    @Test
    fun `two notes that share a name are still asked`() = runTest {
        script(options = "Meetings/2026-10-21 Kickoff\\nWork/2026-10-21 Kickoff")

        val steps =
            run(paths = listOf("Meetings/2026-10-21 Kickoff.md", "Work/2026-10-21 Kickoff.md"))

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(
                steps.last() is AgentStep.Asked,
                engine.generated.any { call ->
                    call.any {
                        SAME in it.text
                    }
                }
            )
        )
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val SAME = "are the same note"
    }
}
