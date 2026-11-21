package app.notekea.shell.data

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.notes.domain.RelativePath
import app.notekea.shell.data.chat.TrashLast
import kotlin.test.Test
import kotlin.test.assertEquals

/** A plan's trash steps run last, in one step, unless a later step names what they trash. */
class TrashLastTest {
    @Test
    fun `trash steps go last in one step, a note inside a trashed folder going with the folder`() {
        val append = ActionStep(
            kind = ActionKind.APPEND_TEXT,
            notes = listOf(HOME)
        )

        val ordered = TrashLast.ordered(
            steps = listOf(
                trash(path = IDEAS_NOTE),
                append,
                trash(path = IDEAS)
            )
        )

        assertEquals(
            expected = listOf(
                append,
                trash(path = IDEAS)
            ),
            actual = ordered
        )
    }

    @Test
    fun `a trash a later step names stays where it was written`() {
        val steps = listOf(
            trash(path = IDEAS),
            ActionStep(
                kind = ActionKind.CREATE_FOLDER,
                folder = IDEAS
            )
        )

        assertEquals(
            expected = steps,
            actual = TrashLast.ordered(steps = steps)
        )
    }

    private fun trash(path: RelativePath) = ActionStep(
        kind = ActionKind.TRASH,
        notes = listOf(path)
    )

    private companion object {
        val HOME = RelativePath(value = "Home.md")
        val IDEAS = RelativePath(value = "Ideas")
        val IDEAS_NOTE = RelativePath(value = "Ideas/Arepas.md")
    }
}
