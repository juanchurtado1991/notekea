package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.core.notes.domain.NoteFixtures.entry
import kotlin.test.Test
import kotlin.test.assertEquals

/** A new note's title that says its folder too (dev, 2026-11-24): the note was created under a name no step found. */
class NewNoteTitleTest {
    private val notes = listOf("Diary/2026-11-10.md", "Music/Queen.md").map { entry(path = it) }

    private fun placed(
        title: String,
        folder: String? = null,
        kind: ActionKind = ActionKind.CREATE_NOTE
    ): List<String?> {
        val inFolder = folder
            ?.let { mapOf(ActionField.FOLDER to it) }
            .orEmpty()

        val step = NewNoteTitle.placed(
            step = PlannedStep(
                kind = kind,
                fields = inFolder + (ActionField.TITLE to title)
            ),
            notes = notes
        )

        return listOf(step[ActionField.TITLE], step[ActionField.FOLDER])
    }

    @Test
    fun `a title with its folder is the title in that folder, written with it or alone`() {
        assertEquals(
            expected = List(size = 2) { listOf<String?>("2026-11-11", "Diary") },
            actual = listOf(
                placed(
                    title = "Diary/2026-11-11",
                    folder = "Diary"
                ),
                placed(title = "Diary/2026-11-11")
            )
        )
    }

    @Test
    fun `a title whose first part is no folder, another folder asked for, and a change of no title stay as written`() {
        assertEquals(
            expected = listOf(
                listOf("AC/DC tabs", null),
                listOf("Diary/2026-11-11", "Music"),
                listOf<String?>("Diary/2026-11-11", null)
            ),
            actual = listOf(
                placed(title = "AC/DC tabs"),
                placed(
                    title = "Diary/2026-11-11",
                    folder = "Music"
                ),
                placed(
                    title = "Diary/2026-11-11",
                    kind = ActionKind.RENAME
                )
            )
        )
    }
}
