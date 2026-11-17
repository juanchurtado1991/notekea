package app.notekea.shell.data

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's changes as the bug hunt of 2026-11-17 found them done (T13, T15, A3, A6, A10, A11): a task line kept the
 * model's own marker, ticks read a stale list, Undo trashed a daily note the person wrote in, a tag on a note that was
 * not there said done, a link went to either of two notes, and a note from a template or the trash stayed.
 */
class VaultActionRunnerFixesTest {
    private val fixture = ChatRunnerFixture()
    private val store = fixture.store
    private val notes = listOf(entry(path = HOME.value))

    @Test
    fun `a task is one line under a list, a blank note gets no gap, and a code block is closed first`() =
        runTest {
            store.texts[HOME] = "# Home\n\n- [ ] a\n"
            store.texts[BLANK] = "\n"
            store.texts[CODE] = "```\nprint(1)\n"

            fixture.run(
                notes,
                task(
                    text = "[ ] Buy milk",
                    due = "2027-02-30"
                ),
                task(
                    text = "- [x] Call\nmom",
                    due = "2027-02-03"
                ),
                appended(
                    note = BLANK,
                    text = "First"
                ),
                appended(
                    note = CODE,
                    text = "After"
                )
            )

            assertEquals(
                expected = listOf(
                    "# Home\n\n- [ ] a\n- [ ] Buy milk\n- [ ] Call mom 📅 2027-02-03\n",
                    "First\n",
                    "```\nprint(1)\n```\n\nAfter\n"
                ),
                actual = listOf(store.texts[HOME], store.texts[BLANK], store.texts[CODE])
            )
        }

    @Test
    fun `a task the plan added is ticked after it, and two identical lines are both ticked`() =
        runTest {
            store.texts[HOME] = "# Home\n\n- [ ] Water\n- [ ] Water\n"

            fixture.run(
                notes,
                task(text = "Call mom"),
                done(text = "Call mom"),
                done(text = "Water"),
                done(text = "Water")
            )

            assertEquals(
                expected = "# Home\n\n- [x] Water\n- [x] Water\n- [x] Call mom\n",
                actual = store.texts[HOME]
            )
        }

    @Test
    fun `a daily note the plan made is trashed by Undo only while the person has not written in it`() =
        runTest {
            val untouched = ChatRunnerFixture()
            store.texts[DAILY] = ""
            untouched.store.texts[DAILY] = ""

            val written = fixture.run(notes, dailyTask())
            val left = untouched.run(notes, dailyTask())

            store.texts[DAILY] = store.texts[DAILY] + "Mine\n"
            fixture.undo(done = written)
            untouched.undo(done = left)

            assertEquals(
                expected = listOf(emptyList<RelativePath>(), listOf(DAILY)),
                actual = listOf(
                    fixture
                        .trash
                        .trashed
                        .toList(),
                    untouched
                        .trash
                        .trashed
                        .toList()
                )
            )
        }

    @Test
    fun `a property on a note not there fails, and a link to a shared name is written by its path`() =
        runTest {
            store.texts[HOME] = "# Home\n"
            val shared = notes + listOf("Books/Dune.md", "Old/Dune.md").map { entry(path = it) }

            val missing = fixture.run(
                notes,
                ActionStep(
                    kind = ActionKind.SET_PROPERTY,
                    notes = listOf(RelativePath(value = "Gone.md")),
                    property = "status",
                    value = "done"
                )
            )

            fixture.run(
                shared,
                ActionStep(
                    kind = ActionKind.LINK_NOTES,
                    notes = listOf(HOME),
                    target = RelativePath(value = "Books/Dune.md")
                )
            )

            assertEquals(
                expected = listOf<Any?>(
                    Outcome.Failure(error = ActionProblem.UnknownNote(name = "Gone")),
                    "# Home\n\n[[Books/Dune]]\n"
                ),
                actual = listOf(missing, store.texts[HOME])
            )
        }

    @Test
    fun `a note from a template goes with Undo, and a move or a trash that cannot be done fails`() =
        runTest {
            store.texts[HOME] = "# Home\n"
            store.texts[TEMPLATE] = "## Agenda\n"
            fixture.operations.existing += RelativePath(value = "Archive/Home.md")

            val made = fixture.run(
                notes,
                ActionStep(
                    kind = ActionKind.CREATE_NOTE,
                    title = "Standup",
                    template = TEMPLATE
                )
            )

            fixture.undo(done = made)

            val moved = fixture.run(
                notes,
                ActionStep(
                    kind = ActionKind.MOVE,
                    notes = listOf(HOME),
                    target = RelativePath(value = "Archive")
                )
            )

            val refused = NotesError.OperationFailed(path = HOME.value)
            fixture.trash.outcome = Outcome.Failure(error = refused)

            val trashed = fixture.run(
                notes,
                ActionStep(
                    kind = ActionKind.TRASH,
                    notes = listOf(HOME)
                )
            )

            assertEquals(
                expected = listOf(true, true, true),
                actual = listOf(
                    RelativePath(value = "Standup.md") in fixture.trash.trashed,
                    moved is Outcome.Failure,
                    trashed is Outcome.Failure
                )
            )
        }

    // A11: a trash cannot be taken back, so a plan that failed after one left the note in the trash.
    @Test
    fun `a plan whose last step fails leaves the note it trashed in place`() = runTest {
        fixture.operations.existing += HOME

        val failed = fixture.run(
            notes,
            ActionStep(
                kind = ActionKind.TRASH,
                notes = listOf(HOME)
            ),
            ActionStep(
                kind = ActionKind.MOVE,
                notes = listOf(BLANK),
                target = RelativePath(value = "..")
            )
        )

        assertEquals(
            expected = listOf<Any>(true, emptyList<RelativePath>()),
            actual = listOf(
                failed is Outcome.Failure,
                fixture
                    .trash
                    .trashed
                    .toList()
            )
        )
    }

    private fun task(
        text: String,
        due: String? = null
    ) = ActionStep(
        kind = ActionKind.ADD_TASK,
        notes = listOf(HOME),
        text = text,
        due = due
    )

    private fun dailyTask() = ActionStep(
        kind = ActionKind.ADD_TASK,
        text = "Call mom"
    )

    private fun done(text: String) = ActionStep(
        kind = ActionKind.COMPLETE_TASK,
        notes = listOf(HOME),
        text = text
    )

    private fun appended(
        note: RelativePath,
        text: String
    ) = ActionStep(
        kind = ActionKind.APPEND_TEXT,
        notes = listOf(note),
        text = text
    )

    private companion object {
        val HOME = RelativePath(value = "Home.md")
        val BLANK = RelativePath(value = "Blank.md")
        val CODE = RelativePath(value = "Code.md")
        val DAILY = RelativePath(value = "Journal/2026-11-10.md")
        val TEMPLATE = RelativePath(value = "Templates/Meeting.md")
    }
}
