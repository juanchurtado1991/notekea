package app.notekea.core.domain.time

/** A moment as the user's clock on the wall shows it: a [date] and the [hour] (0 to 23), [minute] and [second]. */
data class LocalMoment(
    val date: CalendarDate,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0
)
