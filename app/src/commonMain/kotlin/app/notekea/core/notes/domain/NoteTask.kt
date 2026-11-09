package app.notekea.core.notes.domain

import app.notekea.core.domain.time.CalendarDate

/**
 * A task of a note, as the note cache keeps it: [stateIndex] is the character between its brackets
 * and [endIndex] the end of its line, both in the note's body; its [text] as it reads, under its [heading], with the
 * [date] written in it, if any.
 */
data class NoteTask(
    val stateIndex: Int,
    val endIndex: Int,
    val text: String,
    val isDone: Boolean,
    val heading: String? = null,
    val date: CalendarDate? = null
)
