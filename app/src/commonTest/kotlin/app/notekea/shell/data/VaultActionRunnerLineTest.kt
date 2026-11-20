package app.notekea.shell.data

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A line changed by the chat (4.2): written in place at Do it, written back by Undo, and never a guess. */
class VaultActionRunnerLineTest {
    private val fixture = ChatRunnerFixture()
    private val store = fixture.store
    private val notes = listOf(entry(path = DAY.value))

    private fun changed(
        text: String,
        line: String
    ) = ActionStep(
        kind = ActionKind.CHANGE_LINE,
        notes = listOf(DAY),
        line = line,
        text = text
    )

    @Test
    fun `the line is changed in place, and Undo writes it back`() = runTest {
        val before = "# Friday\n- 10:00 Interview with Ana\n- 12:00 Lunch\n"
        store.texts[DAY] = before

        val done = fixture.run(
            notes,
            changed(
                line = "- 10:00 Interview with Ana",
                text = "- 16:00 Interview with Ana"
            )
        )

        val after = store.texts[DAY]
        fixture.undo(done = done)

        assertEquals(
            expected = listOf("# Friday\n- 16:00 Interview with Ana\n- 12:00 Lunch\n", before),
            actual = listOf(after, store.texts[DAY])
        )
    }

    @Test
    fun `a line no longer in the note fails the plan and leaves the note as it was`() = runTest {
        val before = "# Friday\n- 12:00 Lunch\n"
        store.texts[DAY] = before

        val done = fixture.run(
            notes,
            changed(
                line = "- 10:00 Interview with Ana",
                text = "- 16:00 Interview with Ana"
            )
        )

        assertEquals(
            expected = listOf(
                Outcome.Failure(error = ActionProblem.UnknownLine(text = "- 10:00 Interview with Ana")),
                before
            ),
            actual = listOf(done, store.texts[DAY])
        )
    }

    private companion object {
        val DAY = RelativePath(value = "Journal/2026-11-20.md")
    }
}
