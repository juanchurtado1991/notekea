package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How a turn of the loop ends, now that it is the chat's only way (`docs/agent-loop.md`, "Step 6, finished"): its
 * answer shown as it is written and ended with the sources of the notes it names, and a model that cannot be run said
 * as the model's failure, not as a turn that stopped.
 */
class LoopEndTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)
    private val arepas = RelativePath(value = "Recipes/Arepas.md")

    private val runTurn = loopTurn(
        engine = engine,
        cache = cache,
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.ASK to listOf(
                    ReadTool(noteTextCache = cache),
                    AnswerTool()
                )
            )
        )
    )

    private suspend fun stepsOf(task: String): List<AgentStep> = runTurn(
        task = task,
        vaultPath = VAULT,
        notes = listOf(entry(path = arepas.value)),
        context = ActionContext(
            today = "2026-11-27",
            notes = listOf("Recipes/Arepas")
        ),
        openNote = null
    ).toList()

    @Test
    fun `an answer is shown as it is written, then ends with the note it names as its source`() =
        runTest {
            store.texts[arepas] = "Mix 2½ cups of warm water with the flour."
            var calls = 0

            engine.reply = { messages ->
                val asked = messages.first().text

                when {
                    asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""
                    asked.startsWith(prefix = CALLS) && calls++ == 0 -> """{"tool":"read","note":"Arepas"}"""
                    asked.startsWith(prefix = CALLS) -> """{"tool":"answer","text":"2½ cups."}"""
                    asked.startsWith(prefix = "Write the answer") -> ANSWER
                    else -> ""
                }
            }

            val steps = stepsOf(task = "how much water do the arepas take?")
            val last = steps.last() as AgentStep.Finished

            assertEquals(
                expected = listOf<Any?>(true, ANSWER, listOf(arepas)),
                actual = listOf(
                    steps.any {
                        it is AgentStep.Writing && it.answer == ANSWER
                    },
                    last.summary,
                    last.sources.map { it.note }
                )
            )
        }

    @Test
    fun `the not found reply is never shown as it is written`() = runTest {
        store.texts[arepas] = "Mix the flour with water."
        var calls = 0

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""
                asked.startsWith(prefix = CALLS) && calls++ == 0 -> """{"tool":"read","note":"Arepas"}"""
                asked.startsWith(prefix = CALLS) -> """{"tool":"answer","text":"Not there."}"""
                asked.startsWith(prefix = "Write the answer") -> "NOT_FOUND"
                else -> ""
            }
        }

        val steps = stepsOf(task = "when do the arepas go in the oven?")

        assertEquals(
            expected = emptyList(),
            actual = steps.filter {
                it is AgentStep.Writing && it.answer != null
            }
        )
    }

    @Test
    fun `a model that cannot be run ends the turn as its failure`() = runTest {
        engine.failure = LlmError.EngineFailed

        assertEquals(
            expected = AgentStep.Failed(error = LlmError.EngineFailed),
            actual = stepsOf(task = "how much water do the arepas take?").last()
        )
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ANSWER = "The Arepas note says 2½ cups of warm water."
    }
}
