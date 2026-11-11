package app.notekea.cards.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.RelativePath

/**
 * A flashcard of a note (4.6), in the format of Obsidian's Spaced Repetition plugin: `Question::Answer` on one line,
 * its schedule ([due], [interval] in days, [ease] in percent) in a `<!--SR:...-->` comment after it; a new card has
 * none. [line] is where it is in the [note], from 0.
 */
data class StudyCard(
    val note: RelativePath,
    val line: Int,
    val question: String,
    val answer: String,
    val due: CalendarDate? = null,
    val interval: Int = 0,
    val ease: Int = CardSchedule.START_EASE
) {
    val isNew: Boolean get() = due == null

    fun isDue(today: CalendarDate): Boolean = due == null || due <= today
}
