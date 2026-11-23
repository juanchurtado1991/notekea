package app.notekea.chat.domain.tool.change

import app.notekea.chat.data.GhostModelJson
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What a change tool makes of the arguments a model writes (the bug hunt of 2026-11-17): a value it may write as a
 * number, an object around it, a refusal that says what is wrong, a key on the whole text and a change corrected in
 * place of the one before.
 */
class ChangeArgumentsTest {
    private fun contextOf(task: String) = ToolContext(
        vaultPath = VAULT,
        notes = listOf("Home.md", "Projects/Trip to Lisbon.md").map { entry(path = it) },
        task = task,
        openNote = null
    )

    private suspend fun said(
        context: ToolContext,
        arguments: Map<String, String>,
        tool: ChangeTool = ChangeTools.setProperty
    ): String = tool
        .run(
            call = ToolCall(
                name = tool.name,
                arguments = arguments
            ),
            context = context
        )
        .forModel

    private fun valueOf(context: ToolContext): String? = context
        .work
        .changes
        .lastOrNull()
        ?.fields
        ?.values
        ?.last()

    // E2: a priority could only be a string, and came out "{2}" or "}".
    @Test
    fun `a value may be a number in the grammar, and a bare number reaches the step as its text`() =
        runTest {
            val context = contextOf(task = "set priority 3 on my Lisbon trip")

            val fields = GhostModelJson().fields(
                json = """{"tool":"set_property","notes":["Projects/Trip to Lisbon"],""" +
                    """"property":"priority","value":3}"""
            )

            val call = ToolCall.of(fields = checkNotNull(value = fields))

            ChangeTools.setProperty.run(
                call = checkNotNull(value = call),
                context = context
            )

            val schema = ToolRegistry(tools = listOf(ChangeTools.setProperty)).schema()

            assertEquals(
                expected = listOf(true, "3"),
                actual = listOf(
                    "\"value\":{\"anyOf\":[{\"type\":\"string\"},{\"type\":\"number\"}," +
                        "{\"type\":\"boolean\"}]}" in schema,
                    valueOf(context = context)
                )
            )
        }

    // E1: `{"value": 2}` became `value": 2`; E3: a refusal printed `Missing(kind=SET_PROPERTY, field=VALUE)`.
    @Test
    fun `an object around a value is its value, and braces alone are refused quoting what was written`() =
        runTest {
            val context = contextOf(task = "set priority 2 on my Lisbon trip")
            val trip = "Projects/Trip to Lisbon"

            said(
                context = context,
                arguments = mapOf(
                    "notes" to trip,
                    "property" to "priority",
                    "value" to "{\"value\": 2}"
                )
            )

            val braces = said(
                context = contextOf(task = "set priority 2 on my Lisbon trip"),
                arguments = mapOf(
                    "notes" to trip,
                    "property" to "priority",
                    "value" to "}"
                )
            )

            val unknown = said(
                context = contextOf(task = "tag Tacos as home"),
                arguments = mapOf(
                    "notes" to "Tacos",
                    "tag" to "home"
                ),
                tool = ChangeTools.addTag
            )

            assertEquals(
                expected = listOf(
                    "2",
                    "set_property: not added. The value written, \"}\", holds no value",
                    "add_tag: not added, no note is named \"Tacos\". Correct it"
                ),
                actual = listOf(
                    valueOf(context = context),
                    braces.substringBefore(delimiter = ": write"),
                    unknown.substringBefore(delimiter = " and call")
                )
            )
        }

    // L11, E9: two texts alike in their first 60 characters were one call, and a correction was added beside.
    @Test
    fun `the repeat key is the whole text, and a change on more notes replaces the one it corrects`() =
        runTest {
            val context = contextOf(task = "set status to done on Home and my Lisbon trip")
            val registry = ToolRegistry(tools = listOf(ChangeTools.append))
            val start = "a".repeat(n = 70)

            val keys = listOf("$start one", "$start two").map {
                registry.keyOf(
                    call = ToolCall(
                        name = "append",
                        arguments = mapOf(
                            "note" to "Home",
                            "text" to it
                        )
                    ),
                    context = context
                )
            }

            listOf("Home", "Home\nProjects/Trip to Lisbon").forEach {
                said(
                    context = context,
                    arguments = mapOf(
                        "notes" to it,
                        "property" to "status",
                        "value" to "done"
                    )
                )
            }

            assertEquals(
                expected = listOf<Any>(2, listOf(listOf("Home", "Projects/Trip to Lisbon"))),
                actual = listOf(
                    keys.distinct().size,
                    context
                        .work
                        .changes
                        .map { it.notes }
                )
            )
        }

    @Test
    fun `a correction says it replaces the earlier change`() = runTest {
        val context = contextOf(task = "set status to done on Home and my Lisbon trip")

        val answers = listOf("Home", "Home\nProjects/Trip to Lisbon").map {
            said(
                context = context,
                arguments = mapOf(
                    "notes" to it,
                    "property" to "status",
                    "value" to "done"
                )
            )
        }

        assertEquals(
            expected = listOf(false, true),
            actual = answers.map {
                REPLACED in it
            }
        )
    }

    private companion object {
        const val REPLACED = "It replaces the earlier set_property, Home, status, done."
    }
}
