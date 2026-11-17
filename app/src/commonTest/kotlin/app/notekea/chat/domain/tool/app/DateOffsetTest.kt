package app.notekea.chat.domain.tool.app

import app.notekea.core.domain.time.CalendarDate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A span before or after a written day: the loop's eighth run asked for "una semana antes de 2026-04-03" and got
 * 2026-04-03 itself, so the task to change euros a week before leaving was dated the day of the trip.
 */
class DateOffsetTest {
    private val today = CalendarDate(
        year = 2026,
        month = 11,
        day = 17
    )

    private fun dayOf(phrase: String): String? = DatePhrases
        .of(
            phrase = phrase,
            today = today
        )
        ?.let { DatePhrases.written(date = it) }

    @Test
    fun `a week before a written day is seven days before it, and the day alone is itself`() {
        assertEquals(
            expected = listOf(
                "2026-03-27 (Friday)",
                "2026-04-06 (Monday)",
                "2026-05-03 (Sunday)",
                "2026-04-03 (Friday)"
            ),
            actual = listOf(
                "una semana antes de 2026-04-03",
                "3 days after 2026-04-03",
                "a month after 2026-04-03",
                "2026-04-03"
            ).map { dayOf(phrase = it) }
        )
    }

    // T11: today is Tuesday 2026-11-17; these named the day a week on, the coming Friday and this day next month.
    @Test
    fun `a weekday of next week, the last one gone, and the end of next month are those days`() {
        assertEquals(
            expected = listOf(
                "2026-11-27 (Friday)",
                "2026-11-27 (Friday)",
                "2026-11-13 (Friday)",
                "2026-11-13 (Friday)",
                "2026-12-31 (Thursday)",
                "2026-11-20 (Friday)"
            ),
            actual = listOf(
                "friday next week",
                "el viernes de la próxima semana",
                "last friday",
                "el viernes pasado",
                "end of next month",
                "next friday"
            ).map { dayOf(phrase = it) }
        )
    }
}
