package app.notekea.cards.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.RelativePath

/**
 * A flashcard of a note (4.6), as Obsidian's Spaced Repetition plugin writes it: `Question::Answer` on one line at
 * [line] of its [note], its schedule ([due], [interval], [ease]) in a `<!--SR:...-->` comment, none when new.
 * [source] is the sentence Notekea made it from, and [isOutdated] says the note no longer writes it.
 */
data class StudyCard(
    val note: RelativePath,
    val line: Int,
    val question: String,
    val answer: String,
    val due: CalendarDate? = null,
    val interval: Int = 0,
    val ease: Int = CardSchedule.START_EASE,
    val source: String? = null,
    val isOutdated: Boolean = false
) {
    val isNew: Boolean get() = due == null

    /** Answered Hard often enough ([CardSchedule.STRUGGLING_EASE]) that a new wording may be remembered better. */
    val isStruggling: Boolean get() = !isNew && ease <= CardSchedule.STRUGGLING_EASE

    fun isDue(today: CalendarDate): Boolean = due == null || due <= today
}
