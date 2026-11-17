package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.tasks.NoteTask
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Changes that could not be done were answered "added" and failed at Do it, and a note used as a template made a copy
 * of it (the bug hunt of 2026-11-17, T3, T7, T12): they are refused before the card, saying why.
 */
class ImpossibleChangeTest {
    private val home = entry(
        path = "Home.md",
        metadata = NoteMetadata.NONE.copy(
            tasks = listOf("Call mom", "Call dentist", "Pay rent").map {
                NoteTask(
                    stateIndex = 0,
                    endIndex = 0,
                    text = it,
                    isDone = false
                )
            }
        )
    )

    private fun contextOf(task: String) = ToolContext(
        vaultPath = VAULT,
        notes = listOf(home) + OTHERS.map { entry(path = it) },
        task = task,
        openNote = null
    )

    private suspend fun said(
        tool: ChangeTool,
        task: String,
        arguments: Map<String, String>
    ): String = tool
        .run(
            call = ToolCall(
                name = tool.name,
                arguments = arguments
            ),
            context = contextOf(task = task)
        )
        .forModel
        .substringBefore(delimiter = ".")

    @Test
    fun `a note where one is, and a task no open task reads like, are refused before the card`() =
        runTest {
            assertEquals(
                expected = listOf(
                    "create_note: not added",
                    "complete_task: not added",
                    "complete_task: not added",
                    "complete_task: added"
                ),
                actual = listOf(
                    said(ChangeTools.createNote, "create a note Shopping list", SHOPPING),
                    said(ChangeTools.completeTask, "mark call as done", task(text = "call")),
                    said(ChangeTools.completeTask, "mark the gym as done", task(text = GYM)),
                    said(ChangeTools.completeTask, "mark pay rent as done", task(text = RENT))
                )
            )
        }

    @Test
    fun `a task no open task reads like is answered with the note's open tasks`() = runTest {
        val answer = ChangeTools
            .completeTask
            .run(
                call = ToolCall(
                    name = "complete_task",
                    arguments = task(text = "Go to the gym")
                ),
                context = contextOf(task = "mark go to the gym as done")
            )
            .forModel

        assertEquals(
            expected = true,
            actual = "Its open tasks:\n- Call mom\n- Call dentist\n- Pay rent\n" in answer
        )
    }

    @Test
    fun `a note is a template only in a templates folder or where the task says template`() =
        runTest {
            assertEquals(
                expected = listOf(
                    "create_note: not added",
                    "create_note: added",
                    "create_note: added"
                ),
                actual = listOf(
                    said(
                        tool = ChangeTools.createNote,
                        task = "create a note Lista de compras",
                        arguments = mapOf(
                            "title" to "Lista de compras",
                            "template" to "Shopping list"
                        )
                    ),
                    said(
                        tool = ChangeTools.createNote,
                        task = "create a note Standup",
                        arguments = mapOf(
                            "title" to "Standup",
                            "template" to "Meeting"
                        )
                    ),
                    said(
                        tool = ChangeTools.createNote,
                        task = "create a note Groceries from the shopping list template",
                        arguments = mapOf(
                            "title" to "Groceries",
                            "template" to "Shopping list"
                        )
                    )
                )
            )
        }

    // T12: the quoted task's own "set" read as another change, every time.
    @Test
    fun `a task whose quoted text holds set or move is added, and one put into a note is asked`() =
        runTest {
            assertEquals(
                expected = listOf("add_task: added", "add_task: added", "add_task: not added"),
                actual = listOf(
                    said(
                        tool = ChangeTools.addTask,
                        task = "add \"set up the router\" to Home",
                        arguments = mapOf(
                            "note" to "Home",
                            "text" to "set up the router"
                        )
                    ),
                    said(
                        tool = ChangeTools.addTask,
                        task = "add move the couch to my Home note",
                        arguments = mapOf(
                            "note" to "Home",
                            "text" to "move the couch"
                        )
                    ),
                    said(
                        tool = ChangeTools.addTask,
                        task = "add the tag couch to Home",
                        arguments = mapOf(
                            "note" to "Home",
                            "text" to "tag couch"
                        )
                    )
                )
            )
        }

    private fun task(text: String) = mapOf(
        "note" to "Home",
        "text" to text
    )

    private companion object {
        const val GYM = "Go to the gym"
        const val RENT = "**Pay rent**"
        val SHOPPING = mapOf("title" to "Shopping list")
        val OTHERS = listOf("Shopping list.md", "Templates/Meeting.md")
    }
}
