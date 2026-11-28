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

/** An answer from search results alone: the loop reads the note the search showed, and the model answers from it. */
class SearchedUnreadTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)
    private val readings = RelativePath(value = "Health/Glucose.md")
    private val embedder = FakeTextEmbedder()
    private val index = FakeVectorIndex()

    // The E2B's dev set, 2026-11-26: the average of November's readings from the two a search result showed.
    @Test
    fun `an answer given with no note read makes the loop read the note the search showed`() =
        runTest {
            val body = "# Glucose\n\n## November\n- 11-02: 128\n- 11-05: 131\n- 11-09: 129"
            store.texts[readings] = body
            indexed(body = body)
            engine.scoreOf = { _, _ -> 4f }

            var calls = 0

            engine.reply = { messages ->
                val asked = messages.first().text

                when {
                    asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""
                    asked.startsWith(prefix = CALLS) -> CALLED.getOrElse(index = calls++) { ANSWER }
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

            val steps = turn(
                task = "what is the average of my glucose readings in november?",
                vaultPath = VAULT,
                notes = listOf(entry(path = readings.value)),
                context = ActionContext(
                    today = "2026-11-26",
                    notes = listOf("Health/Glucose")
                ),
                openNote = null
            ).toList()

            val moves = steps
                .filterIsInstance<AgentStep.Moving>()
                .map { it.move }

            assertEquals(
                expected = listOf<Any?>(listOf("search", "read"), true, 3),
                actual = listOf(moves, steps.last() is AgentStep.Finished, calls)
            )
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

        val CALLED = listOf(
            """{"tool":"search","query":"glucose november"}""",
            ANSWER,
            ANSWER
        )
    }
}
