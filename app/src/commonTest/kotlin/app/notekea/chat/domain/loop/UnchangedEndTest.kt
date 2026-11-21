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
 * A turn that added no change never says it made one: the loop's eighth run ended "Lowered the rating to 3 for
 * Atomic Habits and Sapiens" with every set_property refused, the summary of an empty proposal shown as done.
 */
class UnchangedEndTest {
    private val engine = FakeLlmEngine()

    private val cache = NoteTextCache(
        contentStore = FakeNoteContentStore(),
        textFolder = TextFolder()
    )

    private val runTurn = RunTurnUseCase(
        llmEngine = engine,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ORGANIZE to listOf(
                    ChangeTools.setProperty,
                    ProposeTool()
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

    @Test
    fun `an empty proposal ends on an answer written knowing nothing was changed, not on its summary`() =
        runTest {
            engine.reply = { messages ->
                val asked = messages.first().text

                when {
                    asked.startsWith(prefix = "You say how a request") -> """{"work":"organize","skill":"none"}"""
                    asked.startsWith(prefix = CALLS) -> PROPOSE
                    else -> SAID
                }
            }

            val last = runTurn(
                task = "lower the rating of Sapiens to 3",
                vaultPath = VAULT,
                notes = listOf(entry(path = "Books/Sapiens.md")),
                context = ActionContext(
                    today = "2026-11-17",
                    notes = listOf("Books/Sapiens")
                ),
                openNote = null
            )
                .toList()
                .last()

            val written = engine
                .generated
                .last()
                .joinToString(separator = "\n") { it.text }

            assertEquals(
                expected = listOf(AgentStep.Finished(summary = SAID), true),
                actual = listOf(last, TurnEnd.NOTHING_CHANGED in written)
            )
        }

    private companion object {
        const val SAID = "I could not change the rating."
        const val CALLS = "You work on a task in the person's notes"
        const val PROPOSE = """{"tool":"propose","summary":"Lowered the rating of Sapiens to 3."}"""
    }
}
