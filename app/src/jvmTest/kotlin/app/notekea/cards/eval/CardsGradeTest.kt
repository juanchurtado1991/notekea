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
}
