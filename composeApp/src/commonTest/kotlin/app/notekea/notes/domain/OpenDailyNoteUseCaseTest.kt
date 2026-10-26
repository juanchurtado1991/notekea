package app.notekea.notes.domain

import app.notekea.core.domain.FakeClock
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.notes.domain.NoteFixtures.VAULT
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class OpenDailyNoteUseCaseTest {
    private val settings = FakeVaultSettingsReader()
    private val operations = FakeNoteFileOperations()
    private val store = FakeNoteContentStore()
    private val calendar = FakeLocalCalendar()
    private val clock = FakeClock(currentTime = Instant.parse(input = "2026-10-26T23:30:00Z"))
    private val openDailyNote = OpenDailyNoteUseCase(
        vaultSettingsReader = settings,
        noteFileOperations = operations,
        noteContentStore = store,
        localCalendar = calendar,
        clock = clock
    )

    @Test
    fun `without Obsidian's settings today's note is created empty in Journal, then opened as it is`() = runTest {
        val first = openDailyNote(vaultPath = VAULT)
        val second = openDailyNote(vaultPath = VAULT)

        assertEquals(
            expected = Triple(
                Outcome.Success(value = FollowedLink(path = TODAY, isNew = true)),
                Outcome.Success(value = FollowedLink(path = TODAY, isNew = false)),
                setOf(RelativePath(value = "Journal"), TODAY)
            ),
            actual = Triple(first, second, operations.existing.toSet())
        )
    }

    @Test
    fun `today is the date where the user is, on either side of midnight in UTC`() = runTest {
        val dates = listOf(-5, 0, 2).map { offset ->
            calendar.offsetHours = offset
            (openDailyNote(vaultPath = VAULT) as Outcome.Success).value.path.value
        }

        assertEquals(
            expected = listOf("Journal/2026-10-26.md", "Journal/2026-10-26.md", "Journal/2026-10-27.md"),
            actual = dates
        )
    }

    @Test
    fun `the vault's folder, format and template are followed, folders in the name created too`() = runTest {
        settings.dailyNotes =
            DailyNoteSettings(folder = "Diary", format = "YYYY/MMMM/dddd D", template = "Templates/Day")
        store.texts[RelativePath(value = "Templates/Day.md")] =
            "# {{title}}\nWritten at {{time}} on {{date:YYYY-MM-DD}}\n"

        val opened = openDailyNote(vaultPath = VAULT, date = CalendarDate(year = 2026, month = 10, day = 20))

        val path = RelativePath(value = "Diary/2026/October/Tuesday 20.md")
        assertEquals(
            expected = Triple(
                Outcome.Success(value = FollowedLink(path = path, isNew = true)),
                setOf("Diary", "Diary/2026", "Diary/2026/October", path.value),
                "# Tuesday 20\nWritten at 23:30 on 2026-10-20\n"
            ),
            actual = Triple(opened, operations.existing.map { it.value }.toSet(), store.texts[path])
        )
    }

    @Test
    fun `a template that cannot be read leaves the note empty, and a refused creation is reported`() = runTest {
        settings.dailyNotes = DailyNoteSettings(folder = "", format = "YYYY-MM-DD", template = "Missing")
        val empty = openDailyNote(vaultPath = VAULT)
        operations.failure = NotesError.OperationFailed(path = "2026-10-27.md")

        val refused = openDailyNote(vaultPath = VAULT, date = CalendarDate(year = 2026, month = 10, day = 27))

        assertEquals(
            expected = Triple(
                Outcome.Success(value = FollowedLink(path = RelativePath(value = "2026-10-26.md"), isNew = true)),
                null,
                Outcome.Failure(error = NotesError.OperationFailed(path = "2026-10-27.md"))
            ),
            actual = Triple(empty, store.texts[RelativePath(value = "2026-10-26.md")], refused)
        )
    }

    companion object {
        private val TODAY = RelativePath(value = "Journal/2026-10-26.md")
    }
}
