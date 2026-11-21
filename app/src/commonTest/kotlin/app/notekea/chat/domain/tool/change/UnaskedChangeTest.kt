package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.FrameCount
import app.notekea.chat.domain.loop.FrameTarget
import app.notekea.chat.domain.loop.RouteWords
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.chat.domain.loop.WorkKind
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A change is what the task asks, in the task's words (the loop's seventh run, 2026-11-17): the 4B added "Task to tag
 * home renovation notes with house" to a note it was asked to tag, tagged with "travel" for "con viaje" after being
 * told once, set a priority of "{2}", and two requests that change the notes were routed to ask.
 */
class UnaskedChangeTest {
    private fun contextOf(
        task: String,
        frame: TaskFrame? = null
    ) = ToolContext(
        vaultPath = VAULT,
        notes = listOf("Home.md", "Projects/Trip to Lisbon.md").map { entry(path = it) },
        task = task,
        openNote = null,
        frame = frame
    )

    private suspend fun said(
        tool: ChangeTool,
        context: ToolContext,
        arguments: Map<String, String>
    ): String = tool
        .run(
            call = ToolCall(
                name = tool.name,
                arguments = arguments
            ),
            context = context
        )
        .forModel
        .substringBefore(delimiter = ".")

    @Test
    fun `a task is not added where the task asks to tag, each time it is tried, and is where it asks for one`() =
        runTest {
            val tagging = contextOf(task = "tag the notes about my home renovation with house")
            val asking = contextOf(task = "add a task to Home to call the plumber")

            val task = mapOf(
                "note" to "Home",
                "text" to "Tag home renovation notes with house"
            )

            assertEquals(
                expected = listOf("add_task: not added", "add_task: not added", "add_task: added"),
                actual = listOf(
                    said(ChangeTools.addTask, tagging, task),
                    said(ChangeTools.addTask, tagging, task),
                    said(ChangeTools.addTask, asking, task)
                )
            )
        }

    @Test
    fun `a tag the task writes is used in place of the model's, and a value loses a broken call's braces`() =
        runTest {
            val context =
                contextOf(task = "¿Puedes etiquetar con viaje todas las notas que escriben la palabra Lisbon?")

            val priority = contextOf(task = "set priority 2 on my Lisbon trip")
            val open = contextOf(task = "give my Lisbon trip a tag that fits")

            said(
                tool = ChangeTools.setProperty,
                context = priority,
                arguments = mapOf(
                    "notes" to "Projects/Trip to Lisbon",
                    "property" to "priority",
                    "value" to "{2}"
                )
            )

            assertEquals(
                expected = listOf("add_tag: added", "viaje", "add_tag: not added yet", "2"),
                actual = listOf(
                    said(ChangeTools.addTag, context, TRAVEL),
                    lastOf(context = context),
                    said(ChangeTools.addTag, open, TRAVEL),
                    lastOf(context = priority)
                )
            )
        }

    // The loop's eighth run: "tag it as urgent" tagged two notes with no question.
    @Test
    fun `tag it with two notes is held, as it points at one`() = runTest {
        assertEquals(
            expected = "add_tag: not added yet",
            actual = said(
                tool = ChangeTools.addTag,
                // "it" is one note, as the frame's model says: the open one.
                context = contextOf(
                    task = "tag it as urgent",
                    frame = TaskFrame(
                        targets = listOf(FrameTarget(note = RelativePath(value = "Home.md"))),
                        count = FrameCount.ONE,
                        candidates = listOf("Home.md", "Projects/Trip to Lisbon.md").map {
                            RelativePath(value = it)
                        }
                    )
                ),
                arguments = mapOf(
                    "notes" to "Home\nProjects/Trip to Lisbon",
                    "tag" to "urgent"
                )
            )
        )
    }

    @Test
    fun `the tag a task names is the word after its last with, or after as, and no word of the sentence`() {
        assertEquals(
            expected = listOf("on-menu", "pending", "urgent", "latina", null, null),
            actual = listOf(
                "tag the recipes the weekly menu uses with on-menu",
                "tag the projects with open tasks with pending",
                "tag it as urgent",
                "busca las recetas venezolanas y etiquétalas con latina",
                "tag the notes I wrote with my sister",
                "add a task to call the bank with Ana"
            ).map { ChangeMisuse.tagNamed(task = it) }
        )
    }

    private fun lastOf(context: ToolContext): String = context
        .work
        .changes
        .single()
        .fields
        .values
        .last()

    @Test
    fun `a request that tags or writes a note is not a question, and a question about them still is`() {
        assertEquals(
            expected = listOf(ORGANIZE, WRITE, ASK, ASK, WorkKind.WEB),
            actual = listOf(
                "busca las recetas venezolanas y etiquétalas con latina" to WorkKind.ASK,
                "compare the two supplier meetings in a note Supplier meetings" to WorkKind.ASK,
                "which notes did I tag with latin?" to WorkKind.ASK,
                "what did I write in a note about the garden?" to WorkKind.ASK,
                "look up arepas online and tag the recipe with latin" to WorkKind.WEB
            ).map { (task, routed) ->
                RouteWords.kindOf(
                    task = task,
                    routed = routed
                )
            }
        )
    }

    private companion object {
        val TRAVEL = mapOf(
            "notes" to "Projects/Trip to Lisbon",
            "tag" to "travel"
        )

        val ORGANIZE = WorkKind.ORGANIZE
        val WRITE = WorkKind.WRITE
        val ASK = WorkKind.ASK
    }
}
