package app.notekea.chat.domain

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.NewNotePlace
import app.notekea.chat.domain.plan.NoteDraft
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** Where a note the chat creates goes (the developer's smoke of 2026-11-14: at the root, without a word). */
class NewNotePlaceTest {
    private val notes = listOf(
        entry(path = "Inbox.md"),
        entry(path = "Projects/Launch/Plan.md"),
        entry(path = "Recipes/Arepas.md")
    )

    @Test
    fun `the folders offered are the root and every folder that holds a note`() {
        assertEquals(
            expected = listOf("", "Projects", "Projects/Launch", "Recipes"),
            actual = NewNotePlace.foldersOf(notes = notes).map { it.value }
        )
    }

    @Test
    fun `a note placed elsewhere is written there, under the same name`() {
        val step = ActionStep(
            kind = ActionKind.WRITE_FROM_NOTES,
            title = "Summary",
            content = listOf(
                NoteDraft(
                    path = RelativePath(value = "Summary.md"),
                    text = "# Summary"
                )
            )
        )

        val placed = NewNotePlace.placed(
            step = step,
            folder = RelativePath(value = "Recipes")
        )

        assertEquals(
            expected = listOf("", "Recipes", "Recipes/Summary.md"),
            actual = listOf(
                NewNotePlace.of(step = step).value,
                NewNotePlace.of(step = placed).value,
                placed
                    .content
                    .single()
                    .path
                    .value
            )
        )
    }

    @Test
    fun `a new note with no folder asked for goes beside the open note, not to the root`() {
        val checked = CheckPlanUseCase()(
            steps = listOf(
                PlannedStep(
                    kind = ActionKind.CREATE_NOTE,
                    fields = mapOf(ActionField.TITLE to "Ideas")
                )
            ),
            notes = notes,
            openNote = RelativePath(value = "Recipes/Arepas.md")
        )

        assertEquals(
            expected = "Recipes",
            actual = (checked as Outcome.Success)
                .value
                .single()
                .folder
                ?.value
        )
    }
}
