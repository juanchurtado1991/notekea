package app.notekea.core.data

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalMoment
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** The same instant is a different day and hour in each time zone, daylight saving time included. */
class JvmLocalCalendarTest {
    @Test
    fun `an instant near midnight UTC falls on the day of each time zone`() {
        val instant = Instant.parse(input = "2026-10-26T23:30:00Z")

        assertEquals(
            expected = listOf(
                LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 26), hour = 23, minute = 30),
                LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 27), hour = 8, minute = 30),
                LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 26), hour = 16, minute = 30),
                LocalMoment(date = CalendarDate(year = 2026, month = 10, day = 27), hour = 0, minute = 30)
            ),
            actual = listOf("UTC", "Asia/Tokyo", "America/Los_Angeles", "Europe/Madrid").map { zone ->
                JvmLocalCalendar(zone = { ZoneId.of(zone) }).momentOf(instant = instant)
            }
        )
    }

    @Test
    fun `the zone is read at every call, so a change of the system's zone is seen at once`() {
        var zone = ZoneId.of("UTC")
        val calendar = JvmLocalCalendar(zone = { zone })
        val instant = Instant.parse(input = "2026-10-26T23:30:00Z")

        val before = calendar.momentOf(instant = instant).date
        zone = ZoneId.of("Asia/Tokyo")

        assertEquals(
            expected = listOf(26, 27).map { CalendarDate(year = 2026, month = 10, day = it) },
            actual = listOf(before, calendar.momentOf(instant = instant).date)
        )
    }
}
