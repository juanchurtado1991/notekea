package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `change_line` (4.2): the line is found in the note before the card, so the card shows the note's own line; a line
 * the note does not write, several alike, or one already as asked is said to the model and not proposed.
 */
class ChangeLineToolTest {
    private val store = FakeNoteContentStore()
    private val day = RelativePath(value = "Journal/2026-11-20.md")

    private val tool = ChangeLineTool(
        noteTextCache = NoteTextCache(
            contentStore = store,
            textFolder = TextFolder()
        )
    )

    private val context = ToolContext(
        vaultPath = VAULT,
        notes = listOf(entry(path = day.value)),
        task = "move the interview with Ana to 16:00",
        openNote = null
    )

    private suspend fun said(
        text: String,
        line: String
    ): String = tool
        .run(
            call = ToolCall(
                name = tool.name,
                arguments = mapOf(
                    "note" to "2026-11-20",
                    "text" to text,
                    "line" to line
                )
            ),
            context = context
        )
        .forModel

    @Test
    fun `the line is proposed as the note writes it, with its marks kept on the new one`() =
        runTest {
            store.texts[day] = "# Friday\n- 10:00 Interview with Ana (call)\n- 12:00 Lunch with Leo\n"

            val said = said(
                text = "10:00 Interview with Ana",
                line = "16:00 Interview with Ana (call)"
            )

            val step = context
                .work
                .changes
                .single()

            assertEquals(
                expected = listOf(
                    "change_line: added.",
                    "- 10:00 Interview with Ana (call)",
                    "- 16:00 Interview with Ana (call)"
                ),
                actual = listOf(
                    said.substringBefore(delimiter = " The proposal"),
                    step[ActionField.TEXT],
                    step[ActionField.LINE]
                )
            )
        }

    @Test
    fun `no line, several alike, or a line already as asked is said and not proposed`() = runTest {
        store.texts[day] = "# Friday\n- 10:00 Call with Ana\n- 15:00 Call with Leo\n"

        val saids = listOf(
            said(
                text = "Interview with Ana",
                line = "16:00 Interview with Ana"
            ),
            said(
                text = "Call with",
                line = "16:00 Call with Ana"
            ),
            said(
                text = "10:00 Call with Ana",
                line = "- 10:00 Call with Ana"
            )
        )

        assertEquals(
            expected = listOf(
                "change_line: not added. No line of Journal/2026-11-20 reads like \"Interview with Ana\". " +
                    "Its lines:\n- 10:00 Call with Ana\n- 15:00 Call with Leo",
                "change_line: not added. 2 lines of Journal/2026-11-20 read like \"Call with\":\n" +
                    "- 10:00 Call with Ana\n- 15:00 Call with Leo",
                "change_line: not added. The line already reads \"- 10:00 Call with Ana\""
            ),
            actual = saids.map {
                it.substringBefore(delimiter = "\nCall").substringBefore(delimiter = ": write")
            }
        )

        assertEquals(
            expected = emptyList(),
            actual = context.work.changes
        )
    }
}
