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
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * The loop held to its frame (`docs/agent-frame.md`), whatever the task's words: an open frame is asked, a settled
 * one is not, and a task about several is answered only once each note is read. No word list decides any of it.
 */
class FrameTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private val home = RelativePath(value = "Home/Budget.md")
    private val work = RelativePath(value = "Work/Budget.md")

    private fun runTurn(frame: TaskFrame) = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(ReadTool(noteTextCache = cache), AskUserTool(), AnswerTool())
            )
        ),
        vaultGuide = VaultGuide(noteTextCache = cache),
        proposal = ProposalCheck(
            checkPlan = CheckPlanUseCase(),
            composeNotes = ComposeNotesUseCase(engine, cache, GhostActionPlanReader())
        ),
        clock = Clock.System,
        frames = FrameSource { _, _, _, _, _ -> frame }
    )

    // The model's first call is [first]; once the loop has told it anything, its next one is [then].
    private fun script(
        first: String,
        then: String
    ) {
        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""
                asked.startsWith(prefix = CALLS) && "(Not" in messages.last().text -> then
                asked.startsWith(prefix = CALLS) -> first
                else -> "The total is 900."
            }
        }
    }

    private suspend fun run(frame: TaskFrame): List<AgentStep> = runTurn(frame = frame)(
        task = "what is the total of the budget?",
        vaultPath = VAULT,
        notes = listOf(entry(path = home.value), entry(path = work.value)),
        context = ActionContext(
            today = "2026-11-17",
            notes = listOf("Home/Budget", "Work/Budget")
        ),
        openNote = null
    ).toList()

    private fun frameOf(
        count: FrameCount,
        notes: List<RelativePath>
    ) = TaskFrame(
        targets = notes.map { FrameTarget(note = it) },
        count = count,
        candidates = listOf(home, work)
    )

    @Test
    fun `an answer where the frame is open is held, and the person is asked with its notes as the options`() =
        runTest {
            script(
                first = ANSWER,
                then = ASK
            )

            val end = run(frame = frameOf(FrameCount.ONE, listOf(home, work))).last()

            assertEquals(
                expected = listOf("Home/Budget", "Work/Budget"),
                actual = (end as? AgentStep.Asked)?.options
            )
        }

    @Test
    fun `a question where the frame says which note is held once, and the turn answers`() =
        runTest {
            script(
                first = ASK,
                then = ANSWER
            )

            val end = run(frame = frameOf(FrameCount.ONE, listOf(work))).last()

            assertEquals(
                expected = true,
                actual = end is AgentStep.Finished
            )
        }

    @Test
    fun `an answer about several notes with one unread is held, and the loop is told which`() =
        runTest {
            script(
                first = ANSWER,
                then = ANSWER
            )

            run(frame = frameOf(FrameCount.SEVERAL, listOf(home, work)))

            assertEquals(
                expected = true,
                actual = engine.generated.any { call ->
                    call.any {
                        "you have not read Home/Budget" in it.text
                    }
                }
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ANSWER = """{"tool":"answer","text":"The total is 900."}"""
        const val ASK =
            """{"tool":"ask_user","question":"Which budget?","options":["Home/Budget","Work/Budget"]}"""
    }
}
