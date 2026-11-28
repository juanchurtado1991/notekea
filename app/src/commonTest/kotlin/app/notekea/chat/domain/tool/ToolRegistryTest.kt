package app.notekea.chat.domain.tool

import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The tools a turn is given (`docs/agent-loop.md`, step 1): what the model is told of them and the grammar that
 * holds its call come from the tools themselves, so a new tool is one class.
 */
class ToolRegistryTest {
    private val read = FakeTool(
        name = "read",
        description = "a note's name; you get its text.",
        arguments = listOf(ToolArgument(name = "note"))
    )

    private val write = FakeTool(
        name = "write_note",
        description = "a new note written from others.",
        arguments = listOf(
            ToolArgument(name = "title"),
            ToolArgument(
                name = "folder",
                isRequired = false
            ),
            ToolArgument(
                name = "notes",
                isList = true
            )
        ),
        effect = ToolEffect.PROPOSES
    )

    private val tasks = FakeTool(
        name = "tasks",
        description = "the open tasks."
    )

    private val registry = ToolRegistry(tools = listOf(read, write, tasks))

    private val context = ToolContext(
        vaultPath = VAULT,
        notes = listOf(entry(path = "Recipes/Arepas.md")),
        task = "read the arepas",
        openNote = null
    )

    @Test
    fun `the prompt lists each tool with its arguments and what it does`() {
        assertEquals(
            expected = "- read (note): a note's name; you get its text.\n" +
                "- write_note (title, folder, notes): a new note written from others.\n" +
                "- tasks: the open tasks.",
            actual = registry.described()
        )
    }

    @Test
    fun `the grammar names the tool first and admits only that tool's arguments`() {
        val text = "{\"type\":\"string\"}"
        val list = "{\"type\":\"array\",\"maxItems\":12,\"items\":{\"type\":\"string\"}}"

        assertEquals(
            expected = "{\"anyOf\":[" +
                "{\"type\":\"object\",\"properties\":{\"tool\":{\"const\":\"read\"},\"note\":$text}," +
                "\"required\":[\"tool\",\"note\"],\"additionalProperties\":false}," +
                "{\"type\":\"object\",\"properties\":{\"tool\":{\"const\":\"write_note\"},\"title\":$text," +
                "\"folder\":$text,\"notes\":$list},\"required\":[\"tool\",\"title\",\"notes\"]," +
                "\"additionalProperties\":false}," +
                "{\"type\":\"object\",\"properties\":{\"tool\":{\"const\":\"tasks\"}}," +
                "\"required\":[\"tool\"],\"additionalProperties\":false}]}",
            actual = registry.schema()
        )
    }

    @Test
    fun `a tool is found by its name, and a name no tool has by none`() = runTest {
        val call = ToolCall(
            name = "read",
            arguments = mapOf("note" to " Arepas ")
        )

        assertEquals(
            expected = listOf<Any?>("read Arepas", null, "Arepas"),
            actual = listOf<Any?>(
                registry["read"]?.run(call, context)?.forModel,
                registry["delete_everything"],
                call.text(name = "note")
            )
        )
    }

    @Test
    fun `a call made twice is told by its tool and what it acts on, however it is written`() {
        val named = ToolCall(
            name = "read",
            arguments = mapOf("note" to "arepas")
        )

        val byPath = ToolCall(
            name = "read",
            arguments = mapOf("note" to "Recipes/Arepas.md")
        )

        assertEquals(
            expected = listOf("read:recipes/arepas", "read:recipes/arepas", "tasks:"),
            actual = listOf(
                registry.keyOf(named, context),
                registry.keyOf(byPath, context),
                registry.keyOf(ToolCall(name = "tasks"), context)
            )
        )
    }

    @Test
    fun `what a turn read is kept apart from what held no text, and blind only when nothing held any`() {
        val reads = TurnReads()
        reads.addEmpty(name = "Scan")
        val blind = reads.blind
        reads.add(note = RelativePath(value = "Recipes/Arepas.md"))
        reads.add(note = RelativePath(value = "Recipes/Arepas.md"))

        assertEquals(
            expected = listOf<Any>(
                listOf("Scan"),
                listOf("Recipes/Arepas.md"),
                emptyList<String>()
            ),
            actual = listOf<Any>(blind, reads.read.map { it.value }, reads.blind)
        )
    }

    // Dev, 2026-11-24: a reason before each call halved the figures worked out wrong, and brought questions back; kept
    // to the tools that work a figure out, it is written after the tool and before its figures.
    @Test
    fun `a reason is a short text the grammar asks for before the rest of the call`() {
        val figures = FakeTool(
            name = "tally",
            description = "figures.",
            arguments = listOf(
                ToolArgument(
                    name = "why",
                    isReason = true
                ),
                ToolArgument(name = "how")
            )
        )

        val reason = "{\"type\":\"string\",\"maxLength\":240}"

        assertEquals(
            expected = "{\"anyOf\":[{\"type\":\"object\",\"properties\":{\"tool\":{\"const\":\"tally\"}," +
                "\"why\":$reason,\"how\":{\"type\":\"string\"}},\"required\":[\"tool\",\"why\",\"how\"]," +
                "\"additionalProperties\":false}]}",
            actual = ToolRegistry(tools = listOf(figures)).schema()
        )
    }
}
