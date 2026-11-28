package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.answer.PassageSearch
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.lookup.SearchTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.index.NoteChunk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A conclusion from text not read whole: the loop reads the note the search showed, or the unread note the call
 * names, and the model works it out again from it.
 */
class UnreadConclusionTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)
    private val readings = RelativePath(value = "Health/Glucose.md")
    private val suppliers = RelativePath(value = "Studio/Suppliers.md")
    private val embedder = FakeTextEmbedder()
    private val index = FakeVectorIndex()

    // The E2B's dev set, 2026-11-26: the average of November's readings from the two a search result showed.
    @Test
    fun `an answer given with no note read makes the loop read the note the search showed`() =
        runTest {
            val body = "# Glucose\n\n## November\n- 11-02: 128\n- 11-05: 131\n- 11-09: 129"
            store.texts[readings] = body
            indexed(body = body)

            val steps = stepsOf(
                task = "what is the average of my glucose readings in november?",
                called = listOf(
                    """{"tool":"search","query":"glucose november"}""",
                    ANSWER,
                    ANSWER
                )
            )

            assertEquals(
                expected = listOf<Any?>(listOf("search", "read"), true),
                actual = listOf(movesOf(steps), steps.last() is AgentStep.Finished)
            )
        }

    // The E2B's blind pool, seed 10, 2026-11-27: it summed "Suppliers" orders it never read, and made the refused
    // tally again.
    @Test
    fun `an answer naming a note not read makes the loop read that note first`() = runTest {
        store.texts[readings] = "# Glucose"
        store.texts[suppliers] = "# Suppliers\n\n- BL-204 — $1,350 — pending"

        val steps = stepsOf(
            task = "how much is pending with suppliers?",
            called = listOf(
                """{"tool":"read","note":"Glucose"}""",
                """{"tool":"answer","text":"Suppliers has 1,350 pending."}""",
                ANSWER
            )
        )

        assertEquals(
            expected = listOf<Any?>(listOf("read", "read"), true),
            actual = listOf(movesOf(steps), steps.last() is AgentStep.Finished)
        )
    }

    // The E2B's blind pool, seed 10, 2026-11-27: a tally of "Deposits" rows, each linking a client, had every
    // client read and the tally refused, one by one.
    @Test
    fun `an answer naming a note only as a note read links it is not held for that note`() =
        runTest {
            store.texts[readings] = "# Glucose"
            store.texts[suppliers] = "# Suppliers\n\n- BL-204 — $1,350 — pending, for [[Glucose]]"

            val steps = stepsOf(
                task = "how much is pending with suppliers?",
                called = listOf(
                    """{"tool":"read","note":"Suppliers"}""",
                    """{"tool":"answer","text":"BL-204, for Glucose: 1,350 pending."}"""
                )
            )

            assertEquals(
                expected = listOf<Any?>(listOf("read"), true),
                actual = listOf(movesOf(steps), steps.last() is AgentStep.Finished)
            )
        }

    // The E2B's blind pool, seed 10, 2026-11-28: Home links [[Suppliers]], with no figure, and the figures were
    // summed from a search, Suppliers never read.
    @Test
    fun `an answer naming a note a note read only links makes the loop read that note first`() =
        runTest {
            store.texts[readings] = "# Glucose\n\nSee [[Suppliers]]."
            store.texts[suppliers] = "# Suppliers\n\n- BL-204 — $1,350 — pending"

            val steps = stepsOf(
                task = "how much is pending with suppliers?",
                called = listOf(
                    """{"tool":"read","note":"Glucose"}""",
                    """{"tool":"answer","text":"Suppliers has 1,350 pending."}""",
                    ANSWER
                )
            )

            assertEquals(
                expected = listOf<Any?>(listOf("read", "read"), true),
                actual = listOf(movesOf(steps), steps.last() is AgentStep.Finished)
            )
        }

    private fun movesOf(steps: List<AgentStep>): List<String> = steps
        .filterIsInstance<AgentStep.Moving>()
        .map { it.move }

    private suspend fun stepsOf(
        task: String,
        called: List<String>
    ): List<AgentStep> {
        engine.scoreOf = { _, _ -> 4f }

        var calls = 0

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""
                asked.startsWith(prefix = CALLS) -> called.getOrElse(index = calls++) { ANSWER }
                else -> "The average is 129."
            }
        }

        val turn = loopTurn(
            engine = engine,
            cache = cache,
            toolsets = Toolsets(
                byKind = mapOf(
                    WorkKind.ASK to listOf(
                        SearchTool(
                            passageSearch = PassageSearch(
                                textEmbedder = embedder,
                                vectorIndex = index,
                                noteTextCache = cache,
                                llmEngine = engine
                            ),
                            noteTextCache = cache
                        ),
                        ReadTool(noteTextCache = cache),
                        AnswerTool()
                    )
                )
            )
        )

        return turn(
            task = task,
            vaultPath = VAULT,
            notes = listOf(entry(path = readings.value), entry(path = suppliers.value)),
            context = ActionContext(
                today = "2026-11-26",
                notes = listOf("Health/Glucose", "Studio/Suppliers")
            ),
            openNote = null
        ).toList()
    }

    private suspend fun indexed(body: String) {
        val chunk = NoteChunk(
            readings,
            0,
            emptyList(),
            body.indices,
            body,
            tokens = 8
        )

        val vector = (embedder.embed(texts = listOf(body)) as Outcome.Success).value.single()
        index.put(VAULT, readings, listOf(chunk), listOf(vector))
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ANSWER = """{"tool":"answer","text":"The average is 129."}"""
    }
}
