package app.notekea.chat.domain.tool.lookup

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
 * A long note read whole shows, after its first part, the headings of the rest and the paragraphs that write the
 * task's words: the loop's eighth run said "the agreement does not say what happens to the data" from the first
 * 2,500 of 8,685 characters.
 */
class LongNoteTest {
    private val store = FakeNoteContentStore()
    private val path = RelativePath(value = "Contracts/Service agreement.md")

    private val read = ReadTool(
        noteTextCache = NoteTextCache(
            contentStore = store,
            textFolder = TextFolder
        )
    )

    @Test
    fun `the rest of a long note is named by its headings, with the paragraphs the task asks about`() =
        runTest {
            val filler =
                "The parties agree to the terms of this agreement in good faith. ".repeat(n = 50)

            store.texts[path] = "# Service agreement\n\n## 1. Fees\n\n$filler\n\n## 9. Data\n\n" +
                "The Client owns the data. The Provider returns it within 15 days of the end.\n\n" +
                "## 10. Notices\n\nNotices are sent by post."

            val said = read.run(
                call = ToolCall(
                    name = ReadTool.NAME,
                    arguments = mapOf(ReadTool.NOTE to "Service agreement")
                ),
                context = ToolContext(
                    vaultPath = VAULT,
                    notes = listOf(entry(path = path.value)),
                    task = "who owns the data in the service agreement?",
                    openNote = null
                )
            ).forModel

            assertEquals(
                expected = listOf(true, true, false),
                actual = listOf(
                    "The rest is under these headings: 1. Fees, 9. Data, 10. Notices." in said,
                    "- The Client owns the data. The Provider returns it within 15 days of the end." in said,
                    "- Notices are sent by post." in said
                )
            )
        }
}
