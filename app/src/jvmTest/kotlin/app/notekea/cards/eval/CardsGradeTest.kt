package app.notekea.cards.eval

import app.notekea.cards.domain.ProposedCard
import kotlin.test.Test
import kotlin.test.assertEquals

/** How the cards' set judges a kept card by the code: its answer held by its sentence, and where that sentence is. */
class CardsGradeTest {
    private fun card(
        answer: String,
        sentence: String
    ) = ProposedCard(
        question = "?",
        answer = answer,
        sentence = sentence
    )

    @Test
    fun `an answer is held when its sentence writes each of its words and figures, accents and case aside`() {
        assertEquals(
            expected = listOf(true, true, false, true, false),
            actual = listOf(
                CardsGrade.isHeld(
                    card(
                        answer = "Lima",
                        sentence = "The capital of Peru is Lima."
                    )
                ),
                CardsGrade.isHeld(
                    card(
                        answer = "el Rímac",
                        sentence = "Lima está junto al rio Rimac"
                    )
                ),
                CardsGrade.isHeld(
                    card(
                        answer = "Cusco",
                        sentence = "The capital of Peru is Lima."
                    )
                ),
                CardsGrade.hasFigures(
                    card(
                        answer = "1,200 USD",
                        sentence = "It cost 1200 USD in all."
                    )
                ),
                CardsGrade.hasFigures(
                    card(
                        answer = "38 days",
                        sentence = "The trip took 380 days."
                    )
                )
            )
        )
    }

    @Test
    fun `a sentence's place and the part of a note several cover, overlaps counted once`() {
        val text = "aaaa bbbb cccc dddd"

        val first = CardsGrade.placeOf(
            sentence = "aaaa bbbb",
            text = text
        )!!

        val second = CardsGrade.placeOf(
            sentence = "BBBB cccc",
            text = text
        )!!

        assertEquals(
            expected = listOf(0.0, 0.75, null, 0.5),
            actual = listOf(
                first.start,
                CardsGrade.coverOf(places = listOf(first, second)),
                CardsGrade.placeOf(
                    sentence = "eeee",
                    text = text
                ),
                CardsGrade.cutOf(
                    text = text,
                    chars = 10
                )
            )
        )
    }

    @Test
    fun `a question is worded from its note in its language, and a translated one is not`() {
        val note = "Second run of the week: calves felt tight after the run, so add more calf work."

        assertEquals(
            expected = listOf(true, false),
            actual = listOf(
                CardsGrade.isWorded(
                    card = ProposedCard(
                        question = "What felt tight after the second run?",
                        answer = "Calves",
                        sentence = note
                    ),
                    text = note
                ),
                CardsGrade.isWorded(
                    card = ProposedCard(
                        question = "Was war ein Problem nach dem Lauf und was soll geändert werden?",
                        answer = "Enge Waden",
                        sentence = note
                    ),
                    text = note
                )
            )
        )
    }
}
