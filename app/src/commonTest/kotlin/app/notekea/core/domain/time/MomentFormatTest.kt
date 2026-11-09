package app.notekea.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals

class MomentFormatTest {
    private val format = MomentFormat()
    private val moment = LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 3), hour = 14, minute = 5)

    @Test
    fun `dates are written with Obsidian's Moment tokens, bracketed text as it is`() {
        assertEquals(
            expected = listOf(
                "2026-10-03",
                "Saturday, October 3rd 2026",
                "2026/10/2026-10-03",
                "26 Q4 Oct 3 Sat Sa 6",
                "Week 40 of 2026, day 276",
                "14:05 2:05 PM pm 02",
                "[YYYY] 2026"
            ),
            actual = listOf(
                "YYYY-MM-DD",
                "dddd, MMMM Do YYYY",
                "YYYY/MM/YYYY-MM-DD",
                "YY [Q]Q MMM D ddd dd d",
                "[Week] W [of] GGGG, [day] DDD",
                "HH:mm h:mm A a hh",
                "[[YYYY]] YYYY"
            ).map { format.format(moment = moment, pattern = it) }
        )
    }

    @Test
    fun `ordinals follow English, the teens included`() {
        assertEquals(
            expected = listOf("1st", "2nd", "3rd", "4th", "11th", "12th", "13th", "21st", "22nd", "23rd", "31st"),
            actual = listOf(1, 2, 3, 4, 11, 12, 13, 21, 22, 23, 31).map {
                format.format(
                    moment = LocalMoment(date = CalendarDate(year = 2026, month = 1, day = it)),
                    pattern = "Do"
                )
            }
        )
    }

    @Test
    fun `a name is read back as the date it was written from, and only then`() {
        val date = CalendarDate(year = 2026, month = 10, day = 3)

        assertEquals(
            expected = listOf(date, date, date, date, null, null, null, null),
            actual = listOf(
                "2026-10-03" to "YYYY-MM-DD",
                "Saturday, October 3rd 2026" to "dddd, MMMM Do YYYY",
                "2026/10/2026-10-03" to "YYYY/MM/YYYY-MM-DD",
                "03 Oct 26" to "DD MMM YY",
                "2026-10-3" to "YYYY-MM-DD",
                "Monday, October 3rd 2026" to "dddd, MMMM Do YYYY",
                "2026-02-30" to "YYYY-MM-DD",
                "Shopping list" to "YYYY-MM-DD"
            ).map { (text, pattern) -> format.parseDate(text = text, pattern = pattern) }
        )
    }
}
