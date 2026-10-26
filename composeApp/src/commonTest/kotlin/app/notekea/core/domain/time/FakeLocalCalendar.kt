package app.notekea.core.domain.time

import kotlin.time.Instant

/** The user's calendar in a time zone [offsetHours] from UTC, without daylight saving, as a fixed-offset zone is. */
class FakeLocalCalendar(var offsetHours: Int = 0) : LocalCalendar {
    override fun momentOf(
        instant: Instant
    ): LocalMoment {
        val seconds = instant.epochSeconds + offsetHours * SECONDS_IN_HOUR
        val secondOfDay = seconds.mod(other = SECONDS_IN_DAY)
        return LocalMoment(
            date = CalendarDate.ofEpochDay(epochDay = seconds.floorDiv(other = SECONDS_IN_DAY)),
            hour = (secondOfDay / SECONDS_IN_HOUR).toInt(),
            minute = (secondOfDay % SECONDS_IN_HOUR / SECONDS_IN_MINUTE).toInt(),
            second = (secondOfDay % SECONDS_IN_MINUTE).toInt()
        )
    }

    companion object {
        private const val SECONDS_IN_DAY = 86_400L
        private const val SECONDS_IN_HOUR = 3_600L
        private const val SECONDS_IN_MINUTE = 60L
    }
}
