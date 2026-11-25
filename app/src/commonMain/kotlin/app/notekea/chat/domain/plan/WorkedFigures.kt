package app.notekea.chat.domain.plan

import app.notekea.chat.domain.tool.app.FigureValues

/**
 * The figures a text may work out from those it writes: what a column of one of its tables or lists adds up to, and
 * the sum or the difference of two of one line. A total the notes never write is right when it is one of these.
 */
internal object WorkedFigures {
    private val LISTED = Regex(pattern = "^\\s*(\\||[-*+]\\s|\\d+[.)]\\s)")
    private val RULE = Regex(pattern = "^[|\\s:-]+$")
    private const val CENT = 0.005

    /** What each column of [text]'s tables and lists adds up to, as far as its figures are among [held]. */
    fun sumsIn(
        text: String,
        held: List<Double>
    ): List<Double> = blocksOf(text = text).flatMap { rows ->
        val columns = rows.maxOf { it.size }

        (0 until columns).mapNotNull { column ->
            val cells = rows
                .mapNotNull { it.getOrNull(index = column) }
                .filter { isAmong(it, held) }

            cells
                .takeIf {
                    it.size > 1
                }
                ?.sum()
        }
    }

    /** Whether [value] is a sum of [text]'s columns, or the sum or the difference of two of the [held] of its line. */
    fun isWorked(
        value: Double,
        line: String,
        text: String,
        held: List<Double>
    ): Boolean {
        val own = FigureValues
            .allIn(text = line)
            .filter { isAmong(it, held) }

        val pairs = own.flatMapIndexed { at, one ->
            own
                .drop(n = at + 1)
                .flatMap { other -> listOf(one + other, one - other, other - one) }
        }

        return isAmong(value, pairs + sumsIn(text, held))
    }

    fun isAmong(
        value: Double,
        values: List<Double>
    ): Boolean = values.any {
        kotlin.math.abs(it - value) < CENT
    }

    // Its tables and lists, each row its figures in the order of its columns: a cell's first one, or a line's own.
    private fun blocksOf(text: String): List<List<List<Double?>>> {
        val blocks = mutableListOf<MutableList<List<Double?>>>(mutableListOf())

        text.lines().forEach { line ->
            when {
                !LISTED.containsMatchIn(input = line) -> blocks += mutableListOf<List<Double?>>()
                !RULE.matches(input = line) -> blocks.last() += rowOf(line = line)
            }
        }

        return blocks.filter {
            it.size > 1
        }
    }

    private fun rowOf(line: String): List<Double?> {
        val isRow = line.trim().startsWith(prefix = "|")
        if (!isRow) return FigureValues.allIn(text = line)

        return line
            .trim()
            .trim('|')
            .split('|')
            .map { FigureValues.firstIn(text = it) }
    }
}
