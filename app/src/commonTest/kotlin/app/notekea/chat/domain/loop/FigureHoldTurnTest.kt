package app.notekea.chat.domain.loop

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.ProposalCheck
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
 * An answer held for what only some tools repair (`docs/loop-goal-plan.md`, step 3.3): the next call may only be one
 * of them, as the 4B, told to, ran out of steps answering again (`holds.py`, the dev run of 2026-11-20).
 */
class FigureHoldTurnTest {
    private val store = FakeNoteContentStore()

    private val engine = FakeLlmEngine()

    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

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
        clock = Clock.System
    )

    // The model makes [scripted] in turn; the schemas of the calls it was offered.
    private suspend fun schemasOf(
        task: String,
        paths: List<String>,
        scripted: List<String>
    ): List<String> {
        val calls = ArrayDeque(elements = scripted)

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> ROUTE
                asked.startsWith(prefix = CALLS) -> calls.removeFirstOrNull() ?: ANSWER
                else -> "The answer."
            }
        }

        runTurn(
            task = task,
            vaultPath = VAULT,
            notes = paths.map { entry(path = it) },
            context = ActionContext(
                today = "2026-11-20",
                notes = paths.map { it.removeSuffix(suffix = ".md") }
            ),
            openNote = null
        ).toList()

        return engine
            .options
            .mapNotNull { it.jsonSchema }
            .filter {
                "\"tool\"" in it
            }
    }

    @Test
    fun `an answer held for a note of its topic it has not read is followed by a read, and then any call`() =
        runTest {
            store.texts[RelativePath(value = "Health/Papa.md")] = "Papa's allergy: see [[Care plan]]."
            store.texts[RelativePath(value = "Health/Care plan.md")] = "Papa's allergy is penicillin."

            val schemas = schemasOf(
                task = "what is papa's allergy?",
                paths = listOf("Health/Papa.md", "Health/Care plan.md"),
                scripted = listOf(
                    """{"tool":"read","note":"Health/Papa"}""",
                    """{"tool":"answer","text":"Papa has no allergy."}""",
                    """{"tool":"read","note":"Health/Care plan"}""",
                    """{"tool":"answer","text":"Penicillin."}"""
                )
            )

            assertEquals(
                expected = listOf(true, false, true),
                actual = listOf("\"answer\"" in schemas[1], "\"answer\"" in schemas[2], "\"answer\"" in schemas[3])
            )
        }

    @Test
    fun `after an answer held for its figures, the next call may only be a figure tool, and then any`() =
        runTest {
            val calls = ArrayDeque(
                elements = listOf(
                    """{"tool":"answer","text":"Kyoto cost 3,624 €."}""",
                    """{"tool":"calculate","expression":"1450 + 610"}""",
                    """{"tool":"answer","text":"Kyoto cost 2060 €."}"""
                )
            )

            engine.reply = { messages ->
                val asked = messages.first().text

                when {
                    asked.startsWith(prefix = "You say how a request") -> ROUTE
                    asked.startsWith(prefix = CALLS) -> calls.removeFirstOrNull() ?: ANSWER
                    else -> "The answer."
                }
            }

            runTurn(
                task = "how much did kyoto cost?",
                vaultPath = VAULT,
                notes = listOf(entry(path = "Travel/Costs.md")),
                context = ActionContext(
                    today = "2026-11-20",
                    notes = listOf("Travel/Costs")
                ),
                openNote = null
            ).toList()

            val schemas = engine
                .options
                .mapNotNull { it.jsonSchema }
                .filter {
                    "\"tool\"" in it
                }

            assertEquals(
                expected = listOf(true, false, true),
                actual = listOf(
                    "\"answer\"" in schemas[0],
                    "\"answer\"" in schemas[1],
                    "\"answer\"" in schemas[2]
                )
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ROUTE = """{"work":"ask","skill":"none"}"""
        const val ANSWER = """{"tool":"answer","text":"Done."}"""
    }
}
