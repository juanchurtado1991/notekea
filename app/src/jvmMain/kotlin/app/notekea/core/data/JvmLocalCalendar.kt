package app.notekea.core.data

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.domain.time.LocalMoment
import java.time.ZoneId
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * [LocalCalendar] with `java.time`, in the time zone [zone] gives: the computer's own, read at every call, so a
 * laptop that travels to another zone gets that zone's date as soon as the system changes it.
 */
class JvmLocalCalendar(
    private val zone: () -> ZoneId = { ZoneId.systemDefault() }
) : LocalCalendar {
    override fun momentOf(
        instant: Instant
    ): LocalMoment {
        val local = instant.toJavaInstant().atZone(zone()).toLocalDateTime()
        return LocalMoment(
            date = CalendarDate(year = local.year, month = local.monthValue, day = local.dayOfMonth),
            hour = local.hour,
            minute = local.minute,
            second = local.second
        )
    }
}
