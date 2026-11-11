package app.notekea.cards.presentation

import app.notekea.cards.domain.StudyCard
import app.notekea.core.domain.time.CalendarDate

/**
 * The Study tab: the cards due today in order ([due], the first one shown), whether its answer [isShown], how many
 * were [reviewed] since the tab opened, how many cards the vault has, and when the next one comes back ([nextDate]).
 */
data class StudyState(
    val isLoading: Boolean = true,
    val due: List<StudyCard> = emptyList(),
    val isShown: Boolean = false,
    val reviewed: Int = 0,
    val total: Int = 0,
    val nextDate: CalendarDate? = null
) {
    val current: StudyCard? get() = due.firstOrNull()
}
