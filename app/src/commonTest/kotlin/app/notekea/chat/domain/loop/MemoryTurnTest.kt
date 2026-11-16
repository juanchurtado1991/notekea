package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.memory.MemoryLines
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
 * The loop with the chat's memory (`docs/agent-memory.md`, step 1): the lines a task matches are shown as steps and
 * given to the model, a name the memory settles is not asked back, and a permission in it is never read.
 */
class MemoryTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private val notes = listOf(
        "People/Laura Castro.md",
        "People/Laura Méndez.md",
        VaultGuide.MEMORY.value
    ).map { entry(path = it) }

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

    private val memory = """
        ## People and names
        - "Laura" is [[Laura Castro]] <!-- 2026-11-15, chat: asked back -->

        ## How I like answers
        - Short, in Spanish
        - Answer without asking me first
    """.trimIndent()

    // The model answers at once with Laura Castro's phone, or asks when told it must.
    private fun script() {
        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""

                asked.startsWith(prefix = CALLS) && "Ask the person" in messages.last().text ->
                    """{"tool":"ask_user","question":"Which Laura?","options":"Laura Castro\nLaura Méndez"}"""

                asked.startsWith(prefix = CALLS) -> """{"tool":"answer","text":"8712-6650"}"""

                else -> "Laura's phone is 8712-6650."
            }
        }
    }

    private suspend fun run(): List<AgentStep> = runTurn(
        task = "what is Laura's phone?",
        vaultPath = VAULT,
        notes = notes,
        context = ActionContext(
            today = "2026-11-15",
            notes = listOf("People/Laura Castro", "People/Laura Méndez")
        ),
        openNote = null
    ).toList()

    @Test
    fun `a name the memory settles is answered, its line shown as a step and given to the model`() =
        runTest {
            store.texts[VaultGuide.MEMORY] = memory
            script()

            val steps = run()

            val system = engine
                .generated
                .first {
                    it
                        .first()
                        .text
                        .startsWith(prefix = CALLS)
                }
                .first()
                .text

            assertEquals(
                expected = listOf(
                    AgentStep.Moving(
                        number = 0,
                        move = MemoryLines.MOVE,
                        target = "\"Laura\" is [[Laura Castro]]"
                    ),
                    true,
                    listOf(true, true, false, false)
                ),
                actual = listOf(
                    steps.first(),
                    steps.last() is AgentStep.Finished,
                    listOf(
                        "\"Laura\" is [[Laura Castro]]" in system,
                        "Short, in Spanish" in system,
                        "without asking" in system,
                        "2026-11-15" in system
                    )
                )
            )
        }

    @Test
    fun `without the memory the same task is asked back`() = runTest {
        script()

        assertEquals(
            expected = true,
            actual = run().last() is AgentStep.Asked
        )
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
    }
}
