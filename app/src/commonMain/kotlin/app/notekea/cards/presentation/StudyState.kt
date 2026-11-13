package app.notekea.cards.presentation

import app.notekea.cards.domain.CardChoices
import app.notekea.cards.domain.StudyCard
import app.notekea.core.domain.time.CalendarDate

/**
 * The Study tab: the cards [due] today in order, the first shown, its answer [isShown], how many [reviewed], and when
 * the next comes back ([nextDate]); with a chat model the card's [choices] (made while [isMakingChoices]) and [chosen].
 */
data class StudyState(
    val isLoading: Boolean = true,
    val due: List<StudyCard> = emptyList(),
    val isShown: Boolean = false,
    val reviewed: Int = 0,
    val total: Int = 0,
    val nextDate: CalendarDate? = null,
    val choices: CardChoices? = null,
    val chosen: Int? = null,
    val isMakingChoices: Boolean = false
) {
    val current: StudyCard? get() = due.firstOrNull()

    /** The place of the current card in this sitting, from 1, and how many the sitting has. */
    val position: Int get() = reviewed + 1
    val sitting: Int get() = reviewed + due.size
}
