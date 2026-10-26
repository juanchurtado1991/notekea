package app.notekea.core.domain.time

import kotlin.time.Instant

/** The user's calendar and wall clock, in the computer's time zone, which only the platform knows. */
interface LocalCalendar {
    /** [instant] as the date and time it is where the user is. */
    fun momentOf(
        instant: Instant
    ): LocalMoment
}
