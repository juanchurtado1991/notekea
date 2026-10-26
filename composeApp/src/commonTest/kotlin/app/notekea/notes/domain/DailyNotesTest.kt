package app.notekea.notes.domain

import app.notekea.core.domain.time.CalendarDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DailyNotesTest {
    private val dailyNotes = DailyNotes()
    private val date = CalendarDate(year = 2026, month = 10, day = 26)

    @Test
    fun `a date's note is its name in the format, in the folder, or at the root when the folder is empty`() {
        assertEquals(
            expected = listOf("Journal/2026-10-26.md", "2026-10-26.md", "Daily/2026/10/Monday 26.md"),
            actual = listOf(
                DailyNoteSettings.DEFAULT,
                DailyNoteSettings(folder = "", format = "YYYY-MM-DD", template = null),
                DailyNoteSettings(folder = "Daily", format = "YYYY/MM/dddd D", template = null)
            ).map { dailyNotes.pathOf(date = date, settings = it).value }
        )
    }

    @Test
    fun `only notes in the folder whose name is a date in the format are daily notes`() {
        assertEquals(
            expected = listOf(date, null, null, null, null),
            actual = listOf(
                "Journal/2026-10-26.md",
                "2026-10-26.md",
                "Journal/Ideas.md",
                "Journal/2026-10-26.pdf",
                "Journal/Old/2026-10-26.md"
            ).map { dailyNotes.dateOf(path = RelativePath(value = it), settings = DailyNoteSettings.DEFAULT) }
        )
    }

    @Test
    fun `a daily note's neighbors are the closest daily notes on each side, skipping missing days`() {
        val notes = listOf(
            "Journal/2026-10-20.md",
            "Journal/2026-10-22.md",
            "Journal/2026-10-26.md",
            "Journal/Ideas.md"
        )
            .map { RelativePath(value = it) }

        assertEquals(
            expected = listOf(
                DayNeighbors(
                    date = CalendarDate(year = 2026, month = 10, day = 22),
                    previous = RelativePath(value = "Journal/2026-10-20.md"),
                    next = RelativePath(value = "Journal/2026-10-26.md")
                ),
                DayNeighbors(date = date, previous = RelativePath(value = "Journal/2026-10-22.md"), next = null),
                null
            ),
            actual = listOf("Journal/2026-10-22.md", "Journal/2026-10-26.md", "Journal/Ideas.md").map {
                dailyNotes.neighborsOf(
                    path = RelativePath(value = it),
                    notes = notes,
                    settings = DailyNoteSettings.DEFAULT
                )
            }
        )
    }
}
