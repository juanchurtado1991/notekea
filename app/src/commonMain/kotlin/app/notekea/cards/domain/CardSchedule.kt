package app.notekea.cards.domain

import app.notekea.core.domain.time.CalendarDate
import kotlin.math.roundToInt

/**
 * When a card comes back (4.6), as Obsidian's Spaced Repetition plugin schedules it (SM-2 with its defaults): Good
 * multiplies the interval by the ease, Easy also by 1.3 and raises the ease, Hard halves the interval and lowers the
 * ease; a new card starts at one day (Hard), three (Good) or four (Easy).
 */
object CardSchedule {
    const val START_EASE = 250
    private const val MIN_EASE = 130
    private const val EASE_STEP = 20
    private const val EASY_BONUS = 1.3
    private const val PERCENT = 100.0
    private val FIRST = mapOf(CardAnswer.HARD to 1, CardAnswer.GOOD to 3, CardAnswer.EASY to 4)

    fun after(
        card: StudyCard,
        answer: CardAnswer,
        today: CalendarDate
    ): StudyCard {
        val ease = when (answer) {
            CardAnswer.HARD -> (card.ease - EASE_STEP).coerceAtLeast(minimumValue = MIN_EASE)
            CardAnswer.GOOD -> card.ease
            CardAnswer.EASY -> card.ease + EASE_STEP
        }

        val interval = if (card.isNew) {
            FIRST.getValue(answer)
        } else {
            when (answer) {
                CardAnswer.HARD -> card.interval * 0.5
                CardAnswer.GOOD -> card.interval * card.ease / PERCENT
                CardAnswer.EASY -> card.interval * card.ease / PERCENT * EASY_BONUS
            }.roundToInt().coerceAtLeast(minimumValue = 1)
        }

        return card.copy(
            due = today.plusDays(days = interval.toLong()),
            interval = interval,
            ease = ease
        )
    }
}
