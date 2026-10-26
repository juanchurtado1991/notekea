package app.notekea.core.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarDateTest {
    @Test
    fun `days count from 1970 both ways and come back to the same date`() {
        val dates = listOf(
            CalendarDate(year = 1970, month = 1, day = 1),
            CalendarDate(year = 1969, month = 12, day = 31),
            CalendarDate(year = 2000, month = 2, day = 29),
            CalendarDate(year = 2026, month = 10, day = 26),
            CalendarDate(year = 1600, month = 3, day = 1)
        )

        assertEquals(
            expected = listOf(0L, -1L, 11_016L, 20_752L, -135_080L) to dates,
            actual = dates.map { it.epochDay } to dates.map { CalendarDate.ofEpochDay(epochDay = it.epochDay) }
        )
    }

    @Test
    fun `moving by days crosses months, leap days and years`() {
        val start = CalendarDate(year = 2024, month = 2, day = 28)

        assertEquals(
            expected = listOf(
                CalendarDate(year = 2024, month = 2, day = 29),
                CalendarDate(year = 2024, month = 3, day = 1),
                CalendarDate(year = 2023, month = 12, day = 31),
                CalendarDate(year = 2025, month = 2, day = 28)
            ),
            actual = listOf(1L, 2L, -59L, 366L).map { start.plusDays(days = it) }
        )
    }

    @Test
    fun `weekday, day of the year and ISO week, including weeks that belong to the next or previous year`() {
        assertEquals(
            expected = listOf(
                Triple(1, 299, 2026 to 44),
                Triple(1, 365, 2025 to 1),
                Triple(5, 1, 2020 to 53),
                Triple(4, 1, 2026 to 1)
            ),
            actual = listOf(
                CalendarDate(year = 2026, month = 10, day = 26),
                CalendarDate(year = 2024, month = 12, day = 30),
                CalendarDate(year = 2021, month = 1, day = 1),
                CalendarDate(year = 2026, month = 1, day = 1)
            ).map { Triple(it.dayOfWeek, it.dayOfYear, it.isoWeek) }
        )
    }

    @Test
    fun `a date is valid only when its month and day exist`() {
        assertEquals(
            expected = listOf(true, false, true, false, false),
            actual = listOf(
                CalendarDate(year = 2024, month = 2, day = 29),
                CalendarDate(year = 2026, month = 2, day = 29),
                CalendarDate(year = 2000, month = 2, day = 29),
                CalendarDate(year = 1900, month = 2, day = 29),
                CalendarDate(year = 2026, month = 4, day = 31)
            ).map { it.isValid }
        )
    }
}
