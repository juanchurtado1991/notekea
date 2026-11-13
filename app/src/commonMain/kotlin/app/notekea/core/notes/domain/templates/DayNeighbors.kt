package app.notekea.core.notes.domain.templates

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.RelativePath

/**
 * The open note as a daily note: its [date], and the daily notes just before and after it in the
 * vault ([previous], [next]), null when there is none on that side.
 */
data class DayNeighbors(
    val date: CalendarDate,
    val previous: RelativePath?,
    val next: RelativePath?
)
