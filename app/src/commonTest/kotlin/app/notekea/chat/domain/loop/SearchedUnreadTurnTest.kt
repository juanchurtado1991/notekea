package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.lookup.SearchTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** An answer from search results alone is held once, and the turn reads the note before it answers. */
class SearchedUnreadTurnTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)
    private val readings = RelativePath(value = "Health/Glucose.md")

    // The E2B's dev set, 2026-11-26: the average of November's readings from the two a search result showed.
    @Test
    fun `an answer given with no note read is held once, and the note is read before the answer`() =
        runTest {
            store.texts[readings] = "# Glucose\n\n## November\n- 11-02: 128\n- 11-05: 131\n- 11-09: 129"

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
                                answerFromNotes = AnswerFromNotesUseCase(
                                    textEmbedder = FakeTextEmbedder(),
                                    vectorIndex = FakeVectorIndex(),
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

            val end = turn(
                task = "what is the average of my glucose readings in november?",
                vaultPath = VAULT,
                notes = listOf(entry(path = readings.value)),
                context = ActionContext(
                    today = "2026-11-26",
                    notes = listOf("Health/Glucose")
                ),
                openNote = null
            ).toList().last()

            val isHeld = engine.generated.any { call ->
                call.any {
                    "you have read no note" in it.text
                }
            }

            assertEquals(
                expected = listOf(true, true, 4),
                actual = listOf(isHeld, end is AgentStep.Finished, calls)
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ANSWER = """{"tool":"answer","text":"The average is 129."}"""

        val CALLED = listOf(
            """{"tool":"search","query":"glucose november"}""",
            ANSWER,
            """{"tool":"read","note":"Health/Glucose"}""",
            ANSWER
        )
    }
}
