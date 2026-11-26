package app.notekea.cards.eval

import app.notekea.cards.domain.CardVerdict
import app.notekea.cards.domain.JudgedCard
import kotlin.math.roundToInt

/** The cards made from one note for the set: every card judged, or why none came, and the note's text. */
internal data class NoteCards(
    val vault: String,
    val note: String,
    val text: String,
    val judged: List<JudgedCard>,
    val problem: String?
) {
    val kept: List<JudgedCard>
        get() = judged.filter {
            it.verdict == CardVerdict.KEPT
        }
}

/**
 * The cards' set read by counts (`docs/gemma-plan.md`, section 8, step 1): what became of the cards proposed, how many
 * of those kept their sentence holds, and how much of each note they cover; for a long note, how many come from
 * past the part the model is given ([cut] characters).
 */
internal class CardsEvalReport(
    private val notes: List<NoteCards>,
    private val cut: Int
) {
    fun of(title: String): String = (
        listOf("# $title", "", totalsOf(), "") +
            verdictsOf() +
            "" +
            vaultsOf() +
            listOf("", "## Each note", "") +
            notes.flatMap { noteOf(note = it) }
        ).joinToString(separator = "\n")

    private fun totalsOf(): String {
        val kept = notes.flatMap { it.kept }
        val held = kept.count { CardsGrade.isHeld(card = it.card) }

        val figured = kept.filter { FIGURE.containsMatchIn(input = it.card.answer) }

        val figures = figured.count { CardsGrade.hasFigures(card = it.card) }
        val made = notes.count { it.kept.isNotEmpty() }
        val proposed = notes.sumOf { it.judged.size }

        return "Notes ${notes.size}, with a card $made; proposed $proposed, kept ${kept.size}, held " +
            "${shareOf(held, kept.size)}, figures held ${shareOf(figures, figured.size)}; cover " +
            "${percentOf(notes.map { coverOf(it) }.average())}."
    }

    private fun verdictsOf(): List<String> {
        val judged = notes.flatMap { it.judged }

        val verdicts = CardVerdict.entries.map { verdict ->
            "| $verdict | ${judged.count { it.verdict == verdict }} |"
        }

        val problems = notes
            .mapNotNull { it.problem }
            .groupingBy { it }
            .eachCount()
            .map { (problem, count) -> "| no cards: $problem | $count notes |" }

        return listOf("| Verdict | Cards |", "|---|---|") + verdicts + problems
    }

    private fun vaultsOf(): List<String> {
        val rows = notes
            .groupBy { it.vault }
            .map { (vault, its) ->
                val kept = its.flatMap { it.kept }
                val held = kept.count { CardsGrade.isHeld(card = it.card) }
                val cover = percentOf(its.map { coverOf(it) }.average())

                "| $vault | ${its.size} | ${kept.size} | $held | $cover | ${its.sumOf { pastOf(it) }} |"
            }

        return listOf("| Vault | Notes | Kept | Held | Cover | Past the cut |", "|---|---|---|---|---|---|") + rows
    }

    private fun noteOf(note: NoteCards): List<String> {
        val made = note.problem ?: "${note.kept.size} of ${note.judged.size} kept"

        val head = "- ${note.vault} / ${note.note} (${note.text.length} chars): $made, cover " +
            "${percentOf(coverOf(note))}, past the cut ${pastOf(note)}"

        val cards = note.kept.map {
            val mark = if (CardsGrade.isHeld(card = it.card)) "held" else "NOT HELD"

            "  - $mark: ${it.card.question} :: ${it.card.answer}"
        }

        return listOf(head) + cards
    }

    private fun coverOf(note: NoteCards): Double = CardsGrade.coverOf(
        places = note.kept.mapNotNull {
            CardsGrade.placeOf(
                sentence = it.card.sentence,
                text = note.text
            )
        }
    )

    // The cards kept whose sentence begins past the part of the note the model is given.
    private fun pastOf(note: NoteCards): Int {
        val end = CardsGrade.cutOf(
            text = note.text,
            chars = cut
        )

        return note.kept.count { card ->
            val place = CardsGrade.placeOf(
                sentence = card.card.sentence,
                text = note.text
            )

            place != null && place.start >= end
        }
    }

    private fun shareOf(
        part: Int,
        whole: Int
    ): String = "$part (${percentOf(if (whole == 0) Double.NaN else part / whole.toDouble())})"

    private fun percentOf(part: Double): String =
        if (part.isNaN()) "–" else "${(part * PERCENT).roundToInt()} %"

    private companion object {
        const val PERCENT = 100
        val FIGURE = Regex(pattern = "\\d")
    }
}
