package app.notekea.chat.domain.tool

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.reading.ListTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A turn's question to the person (`docs/agent-loop.md`, step 4): answers to choose from in place of a guess. */
class WorkToolsTest {
    private val context = ToolContext(
        vaultPath = VAULT,
        notes = emptyList(),
        task = "compare the two leases",
        openNote = null
    )

    @Test
    fun `a question to the person ends the turn with its answers, four at most and each once`() =
        runTest {
            val ask = AskUserTool()

            val call = ToolCall(
                name = ask.name,
                arguments = mapOf(
                    "question" to "Which lease?",
                    "options" to "The flat's\nThe office's\nThe flat's\nBoth\nNeither\nAnother"
                )
            )

            assertEquals(
                expected = listOf(
                    ToolEffect.ENDS,
                    listOf("The flat's", "The office's", "Both", "Neither"),
                    "asked the person: Which lease? [The flat's | The office's | Both | Neither]"
                ),
                actual = listOf(
                    ask.effect,
                    AskUserTool.optionsOf(call = call),
                    ask.run(
                        call = call,
                        context = context
                    ).forModel
                )
            )
        }

    @Test
    fun `the new tools write their lines of the prompt and their grammar as any tool does`() {
        val registry = ToolRegistry(
            tools = listOf(
                ListTool(actionRunner = FakeActionRunner()),
                CalculateTool(),
                AskUserTool()
            )
        )

        val schema = registry.schema()

        assertEquals(
            expected = listOf(true, true, true),
            actual = listOf(
                "- list:" in registry.described() || "list" in registry.described(),
                "\"calculate\"" in schema,
                "\"options\"" in schema
            )
        )
    }
}
