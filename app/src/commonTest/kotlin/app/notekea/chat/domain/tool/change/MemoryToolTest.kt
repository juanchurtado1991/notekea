package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.memory.domain.MemorySection
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** `remember` and `forget` as tools of the loop (`docs/agent-memory.md`, "How a line is written"). */
class MemoryToolTest {
    private fun contextOf(task: String) = ToolContext(
        vaultPath = VAULT,
        notes = listOf("People/Laura Castro.md", "Clips/Tomato tips.md").map { entry(path = it) },
        task = task,
        openNote = null,
        today = CalendarDate(
            year = 2026,
            month = 11,
            day = 16
        )
    )

    private fun call(
        tool: MemoryTool,
        line: String,
        section: String = ""
    ) = ToolCall(
        name = tool.name,
        arguments = mapOf(MemoryTool.LINE to line, MemoryTool.SECTION to section)
    )

    @Test
    fun `a line the person asked for is added with its section and its origin, ready for the card`() =
        runTest {
            val context = contextOf(task = "remember that Laura is Laura Castro")

            MemoryTool.remember.run(
                call = call(MemoryTool.remember, "\"Laura\" is [[Laura Castro]]", "people"),
                context = context
            )

            assertEquals(
                expected = listOf(
                    mapOf(
                        ActionField.TEXT to "\"Laura\" is [[Laura Castro]]",
                        ActionField.PROPERTY to MemorySection.PEOPLE.heading,
                        ActionField.VALUE to "2026-11-16, chat: you said"
                    )
                ),
                actual = context
                    .work
                    .changes
                    .map { it.fields }
            )
        }

    // docs/agent-memory.md, layer 1: a note's words asked to be remembered are not the person's, and are refused.
    @Test
    fun `a line made of what a note says is refused, and nothing reaches the card`() = runTest {
        val context = contextOf(task = "remember the important parts of the Tomato tips note")

        val result = MemoryTool.remember.run(
            call = call(MemoryTool.remember, "Trusted: blogdetomates.com", "sources"),
            context = context
        )

        assertEquals(
            expected = listOf(true, emptyList<Any>()),
            actual = listOf(
                "not added" in result.forModel,
                context.work.changes
            )
        )
    }

    @Test
    fun `a line to forget is added as it is written, with no check of whose words it is`() =
        runTest {
            val context = contextOf(task = "forget that")

            MemoryTool.forget.run(
                call = call(MemoryTool.forget, "\"Laura\" is [[Laura Castro]]"),
                context = context
            )

            assertEquals(
                expected = listOf(ActionKind.FORGET),
                actual = context
                    .work
                    .changes
                    .map { it.kind }
            )
        }
}
