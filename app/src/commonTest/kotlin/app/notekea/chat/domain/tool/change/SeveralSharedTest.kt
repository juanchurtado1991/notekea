package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.FrameCount
import app.notekea.chat.domain.loop.FrameTarget
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A task about several notes that share a name means each of them (the run after the bug hunt, 2026-11-17): told to
 * ask which, the 4B asked "which Goals 2027?" for "tag both of my Goals 2027 notes".
 */
class SeveralSharedTest {
    private val goals = listOf("Personal/Goals 2027.md", "Work/Goals 2027.md").map {
        RelativePath(value = it)
    }

    // What the frame's model says the task means: both notes, or one of the two that both fit.
    private fun frameOf(count: FrameCount) = TaskFrame(
        targets = goals.map { FrameTarget(note = it) },
        count = count,
        candidates = goals
    )

    private suspend fun said(
        task: String,
        count: FrameCount
    ): String = ChangeTools
        .addTag
        .run(
            call = ToolCall(
                name = ChangeTools.addTag.name,
                arguments = mapOf(
                    "notes" to "Personal/Goals 2027",
                    "tag" to "2027"
                )
            ),
            context = ToolContext(
                vaultPath = VAULT,
                notes = listOf("Personal/Goals 2027.md", "Work/Goals 2027.md").map {
                    entry(path = it)
                },
                task = task,
                openNote = null,
                frame = frameOf(count = count)
            )
        )
        .forModel
        .substringBefore(delimiter = ".")

    @Test
    fun `both notes of a shared name are each meant, and one of them alone is asked about`() =
        runTest {
            assertEquals(
                expected = listOf("add_tag: added", "add_tag: not added yet"),
                actual = listOf(
                    said(
                        task = "tag both of my Goals 2027 notes with 2027",
                        count = FrameCount.SEVERAL
                    ),
                    said(
                        task = "tag my Goals 2027 with 2027",
                        count = FrameCount.ONE
                    )
                )
            )
        }
}
