package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.change.ProposeTool
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A pasted text turned into notes (3.9) on the loop, once the planner's only (`docs/agent-loop.md`, step 6): the turn
 * proposes the notes the app writes from the pasted text, a note per topic and the folder's index.
 */
class NotesFromTextTurnTest {
    private val engine = FakeLlmEngine()

    private val runTurn = loopTurn(
        engine = engine,
        cache = NoteTextCache(contentStore = FakeNoteContentStore()),
        toolsets = Toolsets(
            byKind = mapOf(
                WorkKind.WRITE to listOf(
                    ChangeTools.notesFromText,
                    ProposeTool()
                )
            )
        )
    )

    @Test
    fun `a pasted text is proposed as notes written from it, in the folder the call names`() =
        runTest {
            var calls = 0

            engine.reply = { messages ->
                val asked = messages.first().text

                when {
                    asked.startsWith(prefix = "You say how a request") -> """{"work":"write","skill":"none"}"""
                    asked.startsWith(prefix = "You split a text into notes") -> NOTES
                    asked.startsWith(prefix = CALLS) && calls++ == 0 -> """{"tool":"notes_from_text","folder":"Garden"}"""
                    asked.startsWith(prefix = CALLS) -> """{"tool":"propose","summary":"Two notes in Garden."}"""
                    else -> ""
                }
            }

            val last = runTurn(
                task = "make notes of this in a Garden folder:\n\n$PASTED",
                vaultPath = VAULT,
                notes = listOf(entry(path = "Home.md")),
                context = ActionContext(
                    today = "2026-11-27",
                    notes = listOf("Home")
                ),
                openNote = null
            )
                .toList()
                .last()

            val step = (last as AgentStep.Proposed).steps.single()

            assertEquals(
                expected = listOf<Any?>(
                    ActionKind.NOTES_FROM_TEXT,
                    listOf("Garden/Garden.md", "Garden/Tomatoes.md", "Garden/Compost.md")
                ),
                actual = listOf(step.kind, step.content.map { it.path.value })
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"

        const val PASTED = "Tomatoes want six hours of sun and deep watering twice a week.\n\n" +
            "Compost needs greens and browns in equal parts, turned every two weeks."

        const val NOTES =
            """{"notes":[{"title":"Tomatoes","text":"Six hours of sun, deep watering twice a week."},""" +
                """{"title":"Compost","text":"Greens and browns in equal parts, turned every two weeks."}]}"""
    }
}
