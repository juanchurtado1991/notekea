package app.notekea.notes.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import kotlin.test.Test
import kotlin.test.assertEquals

class DailyNoteTemplateTest {
    @Test
    fun `date, time and title are filled in in any case, with their own formats, and other braces stay`() {
        val moment = LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 26), hour = 8, minute = 30)

        assertEquals(
            expected = "# 2026-10-26 (Monday)\nStarted at 08:30, 8 AM\nNote: 2026-10-26\n{{unknown}} 2026-10-26",
            actual = DailyNoteTemplate().fill(
                template = "# {{date}} ({{date:dddd}})\nStarted at {{time}}, {{time:h A}}\nNote: {{title}}\n" +
                    "{{unknown}} {{ DATE }}",
                moment = moment,
                title = "2026-10-26",
                dateFormat = "YYYY-MM-DD"
            )
        )
    }
}
