package app.notekea.cards.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** The cards of a note as Obsidian's Spaced Repetition plugin reads and writes them (4.6). */
class CardFormatTest {
    private val note = RelativePath(value = "Capitals.md")

    @Test
    fun `a note without the tag has no cards, and code and half cards are left out`() {
        val text =
            "#flashcards\n\nCapital of Peru::Lima\n```\nnot::a card\n```\n::no question\n- Capital of Chile::Santiago"

        assertEquals(
            expected = listOf<Any?>(
                emptyList<StudyCard>(),
                listOf("Capital of Peru" to "Lima", "Capital of Chile" to "Santiago"),
                listOf(2, 7)
            ),
            actual = listOf(
                CardFormat.cardsOf(
                    note = note,
                    text = "Capital of Peru::Lima"
                ),
                CardFormat.cardsOf(
                    note = note,
                    text = text
                ).map {
                    it.question to it.answer
                },
                CardFormat.cardsOf(
                    note = note,
                    text = text
                ).map { it.line }
            )
        )
    }

    @Test
    fun `a schedule is read at the end of the line or on the line below`() {
        val text =
            "#flashcards\nA::1 <!--SR:!2026-11-20,3,250-->\nB::2\n<!--SR:!2026-12-01,10,230-->\nC::3"

        val cards = CardFormat.cardsOf(
            note = note,
            text = text
        )

        assertEquals(
            expected = listOf(
                Triple(
                    CalendarDate(
                        year = 2026,
                        month = 11,
                        day = 20
                    ),
                    3,
                    250
                ),
                Triple(
                    CalendarDate(
                        year = 2026,
                        month = 12,
                        day = 1
                    ),
                    10,
                    230
                ),
                Triple(null, 0, CardSchedule.START_EASE)
            ),
            actual = cards.map { Triple(it.due, it.interval, it.ease) }
        )
    }

    @Test
    fun `a schedule is written in place of the one the card had, where it was`() {
        val text =
            "#flashcards\nA::1 <!--SR:!2026-11-20,3,250-->\nB::2\n<!--SR:!2026-12-01,10,230-->\nC::3"

        val cards = CardFormat.cardsOf(
            note = note,
            text = text
        )

        val due = CalendarDate(
            year = 2027,
            month = 1,
            day = 5
        )

        val written = cards
            .fold(initial = text) { current, card ->
                CardFormat.scheduled(
                    text = current,
                    card = card.copy(
                        due = due,
                        interval = 7,
                        ease = 270
                    )
                )
            }

        assertEquals(
            expected = "#flashcards\nA::1 <!--SR:!2027-01-05,7,270-->\nB::2\n<!--SR:!2027-01-05,7,270-->\n" +
                "C::3 <!--SR:!2027-01-05,7,270-->",
            actual = written
        )
    }

    @Test
    fun `new cards go under a heading and the tag, with their separators taken out of the text`() {
        assertEquals(
            expected = "\n\n## Flashcards\n\n#flashcards\n\nWhat is a:b?::it\n",
            actual = CardFormat.blockOf(cards = listOf("What is a::b?" to "it"))
        )
    }

    // The code review of 2026-11-13: a deck tagged in its properties, or "#Flashcards", had no cards.
    @Test
    fun `the deck's tag counts in any case and among the note's properties`() {
        val texts = listOf(
            "#Flashcards\n\nCapital of Peru::Lima",
            "---\ntags: [geography, flashcards]\n---\nCapital of Peru::Lima",
            "---\ntags:\n  - flashcards\n---\nCapital of Peru::Lima",
            "About flashcards in general.\n\nCapital of Peru::Lima"
        )

        assertEquals(
            expected = listOf(1, 1, 1, 0),
            actual = texts.map {
                CardFormat.cardsOf(
                    note = note,
                    text = it
                ).size
            }
        )
    }
}
