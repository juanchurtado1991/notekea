package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.FrameCount
import app.notekea.chat.domain.loop.FrameTarget
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
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

    private fun contextOf(entry: String? = null) = ToolContext(
        vaultPath = VAULT,
        notes = listOf(entry(path = day.value)),
        task = "move the interview with Ana to 16:00",
        openNote = null,
        frame = entry?.let {
            TaskFrame(
                targets = listOf(
                    FrameTarget(
                        note = day,
                        entries = listOf(it)
                    )
                ),
                count = FrameCount.ONE,
                candidates = listOf(day)
            )
        }
    )

    private suspend fun said(
        context: ToolContext,
        line: String,
        text: String
    ): String = tool
        .run(
            call = ToolCall(
                name = tool.name,
                arguments = mapOf(
                    "note" to "2026-11-20",
                    "line" to line,
                    "text" to text
                )
            ),
            context = context
        )
        .forModel

    private suspend fun proposed(
        context: ToolContext,
        line: String,
        text: String
    ): List<String?> {
        val said = said(
            context = context,
            line = line,
            text = text
        )

        val step = context
            .work
            .changes
            .singleOrNull()

        return listOf(
            said.substringBefore(delimiter = " The proposal"),
            step?.get(ActionField.LINE),
            step?.get(ActionField.TEXT)
        )
    }

    @Test
    fun `the line is proposed as the note writes it, with its marks kept on the new one`() =
        runTest {
            store.texts[day] = "# Friday\n- 10:00 Interview with Ana (call)\n- 12:00 Lunch with Leo\n"

            assertEquals(
                expected = listOf(
                    "change_line: added.",
                    "- 10:00 Interview with Ana (call)",
                    "- 16:00 Interview with Ana (call)"
                ),
                actual = proposed(
                    context = contextOf(),
                    line = "10:00 Interview with Ana",
                    text = "16:00 Interview with Ana (call)"
                )
            )
        }

    // The loop's dev run, 2026-11-19: every time change was called with the new line first and the note's line
    // second, refused as no line, and called again until the steps ran out.
    @Test
    fun `the note's line is found whichever argument holds it, or from the frame's entry when it is miscopied`() =
        runTest {
            store.texts[day] = "# Friday\n- 10:00 Interview with Ana ([[Ana]]).\n- 12:00 Lunch with Leo\n"

            val expected = listOf(
                "change_line: added.",
                "- 10:00 Interview with Ana ([[Ana]]).",
                "- 16:00 Interview with Ana ([[Ana]])."
            )

            val called = listOf(
                proposed(
                    context = contextOf(),
                    line = "10:00 Interview with Ana ([[Ana]]).",
                    text = "10:00 Interview with Ana ([[Ana]]). Bring the notes"
                ).last()
            )

            assertEquals(
                expected = listOf(expected, expected, listOf("- 10:00 Interview with Ana ([[Ana]]). Bring the notes")),
                actual = listOf(
                    proposed(
                        context = contextOf(),
                        line = "16:00 Interview with Ana ([[Ana]]).",
                        text = "10:00 Interview with Ana ([[Ana]])."
                    ),
                    proposed(
                        context = contextOf(entry = "- 10:00 Interview with Ana ([[Ana]])."),
                        line = "10:00 Interview with Ana at noon ([[Ana]].",
                        text = "16:00 Interview with Ana ([[Ana]])."
                    ),
                    called
                )
            )
        }

    @Test
    fun `no line, several alike, or a line already as asked is said and not proposed`() = runTest {
        store.texts[day] = "# Friday\n- 10:00 Call with Ana\n- 15:00 Call with Leo\n"
        val context = contextOf()

        val saids = listOf(
            said(
                context = context,
                line = "Interview with Ana",
                text = "16:00 Interview with Ana"
            ),
            said(
                context = context,
                line = "Call with",
                text = "16:00 Call with"
            ),
            said(
                context = context,
                line = "10:00 Call with Ana",
                text = "- 10:00 Call with Ana"
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
                it
                    .substringBefore(delimiter = "\nCall")
                    .substringBefore(delimiter = ": write")
            }
        )

        assertEquals(
            expected = emptyList(),
            actual = context.work.changes
        )
    }

    // The loop's tuned run, 2026-11-19: "baja a 3 el rating" called change_line on "rating: 4" until the steps ran
    // out, and "set date 2026-10-28" wrote the title with a date line under it.
    @Test
    fun `a property and a line with lines added are sent to the tool for them`() = runTest {
        val book = RelativePath(value = "Books/Sapiens.md")
        store.texts[book] = "---\nrating: 4\n---\n# Sapiens\n"

        val context = ToolContext(
            vaultPath = VAULT,
            notes = listOf(
                entry(
                    path = book.value,
                    metadata = NoteMetadata.NONE.copy(
                        properties = listOf(
                            NoteProperty(
                                key = "rating",
                                value = PropertyValue.Number(text = "4")
                            )
                        )
                    )
                )
            ),
            task = "set the rating of Sapiens to 3",
            openNote = null
        )

        val saids = listOf(
            "rating: 4" to "rating: 3",
            "# Sapiens" to "# Sapiens\nDate: 2026-10-28"
        ).map { (line, text) ->
            tool
                .run(
                    call = ToolCall(
                        name = tool.name,
                        arguments = mapOf(
                            "note" to "Sapiens",
                            "line" to line,
                            "text" to text
                        )
                    ),
                    context = context
                )
                .forModel
                .substringAfter(delimiter = "not added. ")
                .substringBefore(delimiter = ":")
        }

        assertEquals(
            expected = listOf(
                "\"rating\" is a property of the note, not a line of it",
                "text is the one line that takes the line's place. To add lines, call append or add_task; to " +
                    "set a property, set_property."
            ),
            actual = saids
        )
    }
}
