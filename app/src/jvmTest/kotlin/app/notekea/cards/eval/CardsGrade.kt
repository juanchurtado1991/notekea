package app.notekea.cards.eval

import app.notekea.cards.domain.ProposedCard
import app.notekea.chat.eval.AnswerGrade

/**
 * How a kept card is judged by the code, with no model (`docs/gemma-plan.md`, section 8): whether its sentence holds
 * its answer, word for word and figure for figure, and where in the note its sentence is. A card whose answer its
 * own sentence does not hold was made up, or taken from elsewhere in the note.
 */
internal object CardsGrade {
    /** Whether [card]'s sentence holds every word and figure of its answer, accents and case aside. */
    fun isHeld(card: ProposedCard): Boolean {
        val sentence = wordsOf(text = card.sentence)

        return wordsOf(text = card.answer).all {
            it in sentence
        }
    }

    /** Whether [card]'s sentence holds every figure of its answer: "38" is held by "38 days", not by "380". */
    fun hasFigures(card: ProposedCard): Boolean {
        val sentence = figuresOf(text = card.sentence)

        return figuresOf(text = card.answer).all {
            it in sentence
        }
    }

    /**
     * Where [sentence] is in [text], from 0 to 1 by its start and end, compared without case, spaces or marks;
     * null when it is not in it.
     */
    fun placeOf(
        sentence: String,
        text: String
    ): ClosedFloatingPointRange<Double>? {
        val plainText = plain(text = text)
        val plainSentence = plain(text = sentence)
        val start = plainText.indexOf(string = plainSentence)

        if (plainSentence.isEmpty() || start < 0) return null

        val length = plainText.length.toDouble()

        return start / length..(start + plainSentence.length) / length
    }

    /** The part of [text] the [places] cover together, from 0 to 1. */
    fun coverOf(places: List<ClosedFloatingPointRange<Double>>): Double {
        var covered = 0.0
        var end = 0.0

        places
            .sortedBy { it.start }
            .forEach { place ->
                val from = maxOf(place.start, end)

                if (place.endInclusive > from) covered += place.endInclusive - from

                end = maxOf(end, place.endInclusive)
            }

        return covered
    }

    /** Where in [text] its first [chars] characters end, from 0 to 1, as [placeOf] measures. */
    fun cutOf(
        text: String,
        chars: Int
    ): Double {
        val whole = plain(text = text).length

        return if (whole == 0) 1.0 else plain(text = text.take(n = chars)).length / whole.toDouble()
    }

    private fun wordsOf(text: String): Set<String> = WORD
        .findAll(input = AnswerGrade.fold(text = text.lowercase()))
        .map { it.value }
        .filter {
            it.length >= MIN_WORD || it.any { char -> char.isDigit() }
        }
        .toSet()

    private fun figuresOf(text: String): Set<String> = FIGURE
        .findAll(
            input = text.replace(
                oldValue = ",",
                newValue = ""
            )
        )
        .map { it.value }
        .toSet()

    private fun plain(text: String): String = text
        .lowercase()
        .filter { it.isLetterOrDigit() }

    private const val MIN_WORD = 3
    private val WORD = Regex(pattern = "[\\p{L}\\p{N}]+")
    private val FIGURE = Regex(pattern = "\\d+(?:[.:]\\d+)*")
}
