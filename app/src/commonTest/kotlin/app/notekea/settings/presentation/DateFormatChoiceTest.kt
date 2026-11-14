package app.notekea.settings.presentation

import app.notekea.core.domain.time.CalendarDate
import app.notekea.settings.presentation.files.DateFormatChoice
import kotlin.test.Test
import kotlin.test.assertEquals

/** The code review of 2026-11-13: on 3 April, "03-04-2026" and "04-03-2026" did not say which was which. */
class DateFormatChoiceTest {
    @Test
    fun `day first and month first are told apart whatever today is`() {
        val early = DateFormatChoice.choicesOf(
            today = CalendarDate(
                year = 2026,
                month = 4,
                day = 3
            ),
            current = ""
        )

        assertEquals(
            expected = listOf("25-04-2026", "04-25-2026"),
            actual = early
                .filter {
                    it.format == "DD-MM-YYYY" || it.format == "MM-DD-YYYY"
                }
                .map { it.example }
        )
    }
}
