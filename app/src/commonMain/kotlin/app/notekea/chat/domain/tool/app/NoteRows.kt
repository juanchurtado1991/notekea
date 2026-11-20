package app.notekea.chat.domain.tool.app

import app.notekea.chat.domain.loop.FrameWords

/**
 * The rows of a note, numbered by the code (`docs/loop-goal-plan.md`, step 3.2): a table row with its cells under the
 * header's names, a list item, and a `Key: value` line that writes a figure, each with its heading. The model names
 * rows by id and the code reads their figures, so no row is copied wrong or left out unseen.
 */
internal object NoteRows {
    class Row(
        val id: String,
        val heading: String,
        val text: String,
        val cells: List<Pair<String, String>>
    )

    /** A row's figure, or why it has none to count. */
    class Figure(
        val problem: String,
        val value: Double?
    )

    fun of(body: String): List<Row> {
        val rows = mutableListOf<Row>()
        var heading = ""
        var header: List<String>? = null
        val lines = body.lines()

        lines.forEachIndexed { at, raw ->
            val line = raw.trim()

            val next = lines
                .getOrNull(index = at + 1)
                ?.trim()
                .orEmpty()

            if (!line.startsWith(prefix = TABLE)) header = null

            when {
                line.startsWith(prefix = HEADING) -> heading = line.trimStart('#').trim()

                line.startsWith(prefix = TABLE) -> {
                    val cells = cellsOf(line = line)

                    when {
                        SEPARATOR.matches(input = line) -> Unit

                        header == null && SEPARATOR.matches(input = next) -> header = cells

                        else -> rows += Row(
                            id = "$ID${rows.size + 1}",
                            heading = heading,
                            text = cells.joinToString(separator = " | "),
                            cells = cells.mapIndexed { index, cell ->
                                header?.getOrNull(index = index).orEmpty() to cell
                            }
                        )
                    }
                }

                else -> rowOf(line = line)?.let {
                    rows += Row(
                        id = "$ID${rows.size + 1}",
                        heading = heading,
                        text = it,
                        cells = emptyList()
                    )
                }
            }
        }

        return rows
    }

    /**
     * The figure of [row] to count: the cell under [column] in a table, the figure closest to the [column] word
     * on another line, else the one figure the row writes. Dates and times are no figures to count.
     */
    fun figureOf(
        row: Row,
        column: String
    ): Figure {
        val wanted = column.trim()

        // The column whose name shares the most words with the one asked: the 4B asked "Distance (mi)" of a
        // column named Distance, and "Hours of sleep" of Hours (the dev run of 2026-11-20).
        val cell = row
            .cells
            .map {
                it to sharedOf(
                    name = it.first,
                    wanted = wanted
                )
            }
            .filter { (_, shared) ->
                shared > 0
            }
            .maxByOrNull { (_, shared) -> shared }
            ?.first

        val text = cell?.second ?: row.text

        val figures = FIGURE
            .findAll(input = plainOf(text = text))
            .mapNotNull { match ->
                FigureValues.valueOf(written = match.value)?.let {
                    match.range.first to it
                }
            }
            .toList()

        val at = row.text.indexOf(
            string = wanted,
            ignoreCase = true
        )

        val chosen = when {
            figures.size == 1 -> figures.single().second

            figures.isEmpty() -> null

            cell == null &&
                wanted.isNotEmpty() &&
                at >= 0 ->
                figures.minBy { (start, _) -> kotlin.math.abs(start - at) }.second

            else -> return Figure(
                value = null,
                problem = "${row.id} writes several figures (${figures.joinToString { written(it.second) }}): " +
                    "call tally with column, the column's name or the word written next to the one that counts."
            )
        }

        return Figure(
            value = chosen,
            problem = if (chosen == null) "${row.id} writes no figure${if (cell != null) " under $wanted" else ""}." else ""
        )
    }

    /** What a row reads as, shown to the model: its cells under their names when it is a table's. */
    fun shownOf(row: Row): String {
        val cells = row
            .cells
            .joinToString(separator = " | ") { (name, cell) ->
                if (name.isEmpty()) cell else "$name: $cell"
            }

        return "${row.id} ${cells.ifEmpty { row.text }}"
    }

    /** Whether [row] is one [word] is about: a column of its table, its heading, or its text writes the word. */
    fun isAbout(
        row: Row,
        word: String
    ): Boolean {
        val words = FrameWords.of(text = word)
        if (words.isEmpty()) return true

        val own =
            FrameWords.of(text = "${row.heading} ${row.text} ${row.cells.joinToString { it.first }}")

        return words.all { one ->
            own.any {
                FrameWords.isSame(
                    one = one,
                    other = it
                )
            }
        }
    }

    // How many words a column's [name] shares with the one [wanted]; the most when they are written alike.
    private fun sharedOf(
        name: String,
        wanted: String
    ): Int {
        val isUnnamed = name.isBlank() || wanted.isBlank()
        if (isUnnamed) return 0
        if (name.equals(other = wanted, ignoreCase = true)) return Int.MAX_VALUE

        val own = FrameWords.of(text = name)

        return FrameWords.of(text = wanted).count { one ->
            own.any {
                FrameWords.isSame(
                    one = one,
                    other = it
                )
            }
        }
    }

    private fun cellsOf(line: String): List<String> = line
        .trim()
        .removePrefix(prefix = TABLE)
        .removeSuffix(suffix = TABLE)
        .split(TABLE)
        .map { it.trim() }

    // A list item, its checkbox left out, or a "Key: value" line that writes a figure; null for any other line.
    private fun rowOf(line: String): String? {
        val item = ITEM
            .matchEntire(input = line)
            ?.groupValues
            ?.get(1)

        if (item != null) {
            return item.replace(
                regex = CHECKBOX,
                replacement = ""
            ).trim()
        }

        val isKeyed =
            KEYED.matches(input = line) && FIGURE.containsMatchIn(input = plainOf(text = line))

        return line.takeIf { isKeyed }
    }

    // The text without its dates and times, which write digits that are no figure to count.
    private fun plainOf(text: String): String = text
        .replace(
            regex = DATE,
            replacement = " "
        )
        .replace(
            regex = TIME,
            replacement = " "
        )

    private fun written(value: Double): String = Arithmetic.written(value = value)

    private const val ID = "R"
    private const val TABLE = "|"
    private const val HEADING = "#"
    private val SEPARATOR = Regex(pattern = "^\\|?\\s*:?-{2,}.*$")
    private val ITEM = Regex(pattern = "^(?:[-*+]|\\d+[.)])\\s+(.+)$")
    private val CHECKBOX = Regex(pattern = "^\\[[ xX]]\\s*")
    private val KEYED = Regex(pattern = "^[^:|#]{1,40}:\\s*\\S.*$")
    private val FIGURE = Regex(pattern = "\\d[\\d.,]*")
    private val DATE =
        Regex(pattern = "\\b\\d{4}-\\d{2}-\\d{2}\\b|\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b")
    private val TIME = Regex(pattern = "\\b\\d{1,2}:\\d{2}\\b")
}
