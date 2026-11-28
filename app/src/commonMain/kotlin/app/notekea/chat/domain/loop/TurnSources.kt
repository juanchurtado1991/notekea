package app.notekea.chat.domain.loop

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.core.notes.domain.RelativePath

/**
 * The sources an answer of the loop is shown with: the passages its searches gave and the notes it read whole, only
 * those of the notes the answer names, numbered in the order it names them (a note read whole opens as its source);
 * and the tables it found that it is from ([tablesOf]).
 */
internal object TurnSources {
    fun of(
        answer: String,
        passages: List<AnswerSource>,
        read: List<RelativePath>
    ): List<AnswerSource> {
        if (answer.isBlank()) return emptyList()

        val passed = passages.map { it.note }.toSet()

        val whole = read
            .filterNot {
                it in passed
            }
            .map {
                AnswerSource(
                    number = 0,
                    note = it,
                    headingPath = emptyList(),
                    range = IntRange.EMPTY,
                    text = ""
                )
            }

        return (passages + whole)
            .distinctBy { it.note }
            .mapNotNull { source ->
                namedAt(
                    answer = answer,
                    name = source.note.nameWithoutExtension
                )?.let {
                    it to source
                }
            }
            .sortedBy { it.first }
            .mapIndexed { index, (_, source) -> source.copy(number = index + 1) }
    }

    /**
     * The tables of [tables] the answer is from, shown under it: those with a row whose note or text it names. The
     * tables of calls the turn corrected on the way ("books rated 4" before "books finished") are left out.
     */
    fun tablesOf(
        answer: String,
        tables: List<FoundNotes>
    ): List<FoundNotes> = tables
        .distinct()
        .filter { table ->
            table.rows.any { row ->
                val isNamed = namedAt(answer, row.note.nameWithoutExtension) != null

                val isQuoted = row.cells.any {
                    it.length >= MIN_CELL && answer.contains(it, ignoreCase = true)
                }

                isNamed || isQuoted
            }
        }

    // A cell shorter than this ("4", "yes") is in many answers that are not from it.
    private const val MIN_CELL = 4

    // Where [answer] first names the note [name] as a whole word: "Art" is not named by "start".
    private fun namedAt(
        answer: String,
        name: String
    ): Int? = Regex(
        pattern = "(?<![\\p{L}\\p{N}])${Regex.escape(literal = name)}(?![\\p{L}\\p{N}])",
        option = RegexOption.IGNORE_CASE
    )
        .find(input = answer)
        ?.range
        ?.first
}
