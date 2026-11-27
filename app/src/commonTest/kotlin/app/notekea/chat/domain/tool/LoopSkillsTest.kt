package app.notekea.chat.domain.tool

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.reading.LinksTool
import app.notekea.chat.domain.tool.reading.ListTool
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What only the planner did, now the loop's tools' (`docs/agent-loop.md`, "Step 6, finished"): the notes with no tag
 * and the broken links listed as the app lists them, the tables found kept for the answer, and a question of a note or
 * a folder that looks only there.
 */
class LoopSkillsTest {
    private val runner = FakeActionRunner()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)

    private val arepas = RelativePath(value = "Recipes/Arepas.md")
    private val lease = RelativePath(value = "Home/Lease.md")

    private val table = FoundNotes(
        columns = listOf("Note"),
        rows = listOf(FoundRow(note = arepas))
    )

    private fun contextOf(scope: Set<RelativePath>? = null) = ToolContext(
        vaultPath = VAULT,
        notes = listOf(entry(path = arepas.value), entry(path = lease.value)),
        task = "which notes have no tag?",
        openNote = null,
        scope = scope
    )

    private fun callOf(vararg arguments: Pair<String, String>) = ToolCall(
        name = "",
        arguments = arguments.toMap()
    )

    @Test
    fun `list with the tag none and links with no note ask the app's own lists, and their tables are kept`() =
        runTest {
            runner.found = table
            val context = contextOf()

            ListTool(actionRunner = runner).run(
                call = callOf(ListTool.TAG to ListTool.NO_TAG),
                context = context
            )

            LinksTool(
                noteTextCache = cache,
                linkFinder = JetBrainsMarkdownLinkFinder(),
                actionRunner = runner
            ).run(
                call = callOf(),
                context = context
            )

            assertEquals(
                expected = listOf<Any>(
                    listOf(ActionKind.LIST_UNTAGGED, ActionKind.LIST_BROKEN_LINKS),
                    listOf(table, table)
                ),
                actual = listOf(runner.asked.map { it.kind }, context.tables)
            )
        }

    @Test
    fun `a question of one note looks only in it, and reads no other note`() = runTest {
        store.texts[lease] = "End it with two months' notice."
        val context = contextOf(scope = setOf(arepas))

        val read = ReadTool(noteTextCache = cache).run(
            call = callOf(ReadTool.NOTE to "Lease"),
            context = context
        )

        assertEquals(
            expected = listOf<Any>(
                listOf(arepas),
                "read \"Lease\": not in the note or the folder this question is asked of; answer from those only."
            ),
            actual = listOf(context.looked.map { it.file.path }, read.forModel)
        )
    }
}
