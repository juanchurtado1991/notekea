package app.notekea.chat.domain.tool

import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.reading.ListTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.chat.domain.tool.work.NoteTool
import app.notekea.chat.domain.tool.work.PlanStep
import app.notekea.chat.domain.tool.work.PlanTool
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A turn's working memory and its question to the person (`docs/agent-loop.md`, step 4): the plan it writes and
 * ticks, the facts it notes with their passages, and a question with answers to choose from in place of a guess.
 */
class WorkToolsTest {
    private val context = ToolContext(
        vaultPath = VAULT,
        notes = emptyList(),
        task = "compare the two leases",
        openNote = null
    )

    private suspend fun said(
        tool: ChatTool,
        vararg arguments: Pair<String, String>
    ): String = tool.run(
        call = ToolCall(
            name = tool.name,
            arguments = arguments.toMap()
        ),
        context = context
    ).forModel

    @Test
    fun `the plan is written whole each time, ticked as it goes, and stays as the turn reads it`() =
        runTest {
            val plan = PlanTool()
            said(plan, "steps" to "Read the first lease\nRead the second\nWrite the table")

            val said =
                said(plan, "steps" to "- [x] Read the first lease\n[ ] Read the second\nWrite the table")

            assertEquals(
                expected = listOf(
                    "plan kept, 1 of 3 done. Next: Read the second",
                    listOf(
                        PlanStep(
                            text = "Read the first lease",
                            isDone = true
                        ),
                        PlanStep(
                            text = "Read the second",
                            isDone = false
                        ),
                        PlanStep(
                            text = "Write the table",
                            isDone = false
                        )
                    ),
                    "Your plan:\n- [x] Read the first lease\n- [ ] Read the second\n- [ ] Write the table"
                ),
                actual = listOf(said, context.work.plan, context.work.described())
            )
        }

    @Test
    fun `a fact is noted once with its passage, and one without a passage is refused`() = runTest {
        val note = NoteTool()
        said(note, "fact" to "The rent is 950 euros", "passage" to "The rent is 950 euros a month.")
        said(note, "fact" to "The rent is 950 euros", "passage" to "950 euros")
        val refused = said(note, "fact" to "It ends in May")

        assertEquals(
            expected = listOf(
                "What you noted:\n- The rent is 950 euros (\"The rent is 950 euros a month.\")",
                "note: write the fact and the passage that says it."
            ),
            actual = listOf(context.work.described(), refused)
        )
    }

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
        val registry =
            ToolRegistry(tools = listOf(ListTool(), CalculateTool(), PlanTool(), AskUserTool()))

        val schema = registry.schema()

        assertEquals(
            expected = listOf(true, true, true, true),
            actual = listOf(
                "- list:" in registry.described() || "list" in registry.described(),
                "\"calculate\"" in schema,
                "\"steps\"" in schema,
                "\"options\"" in schema
            )
        )
    }
}
