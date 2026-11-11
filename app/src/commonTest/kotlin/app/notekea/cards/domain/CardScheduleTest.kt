package app.notekea.cards.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** When a card comes back (4.6), as the Spaced Repetition plugin's SM-2 schedules it. */
class CardScheduleTest {
    private val today = CalendarDate(
        year = 2026,
        month = 11,
        day = 11
    )
    private val new = StudyCard(
        note = RelativePath(value = "A.md"),
        line = 0,
        question = "Q",
        answer = "A"
    )
    private val seen = new.copy(
        due = today,
        interval = 10,
        ease = 250
    )

    private fun after(card: StudyCard, answer: CardAnswer) =
        CardSchedule.after(card, answer, today).let { Triple(it.due, it.interval, it.ease) }

    @Test
    fun `a new card comes back in one, three or four days`() {
        assertEquals(
            expected = listOf(
                Triple(today.plusDays(days = 1), 1, 230),
                Triple(today.plusDays(days = 3), 3, 250),
                Triple(today.plusDays(days = 4), 4, 270)
            ),
            actual = CardAnswer.entries.map {
                after(
                    card = new,
                    answer = it
                )
            }
        )
    }

    @Test
    fun `a card seen before halves on Hard, grows by its ease on Good, and more on Easy`() {
        assertEquals(
            expected = listOf(
                Triple(today.plusDays(days = 5), 5, 230),
                Triple(today.plusDays(days = 25), 25, 250),
                Triple(today.plusDays(days = 33), 33, 270)
            ),
            actual = CardAnswer.entries.map {
                after(
                    card = seen,
                    answer = it
                )
            }
        )
    }

    @Test
    fun `the ease never falls under 130 and the interval never under a day`() {
        val hard = seen.copy(
            interval = 1,
            ease = 140
        )

        assertEquals(
            expected = Triple(today.plusDays(days = 1), 1, 130),
            actual = after(
                card = hard,
                answer = CardAnswer.HARD
            )
        )
    }
}
