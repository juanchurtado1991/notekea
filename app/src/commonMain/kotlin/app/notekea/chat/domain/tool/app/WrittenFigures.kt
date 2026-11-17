package app.notekea.chat.domain.tool.app

/**
 * Every line that writes a word, with the figure it is written with, and what those figures add up to: asked what
 * the fourteen "Spent" of a journal add up to, the 4B read the note, added the first four and answered with that
 * (the loop's fifth run, 2026-11-16). The app gathers them all, so the model neither finds nor adds them.
 */
internal object WrittenFigures {
    /** What count says of [word] in [texts] (each note's name and text), found in [place]; null when none writes it. */
    fun of(
        word: String,
        place: String,
        texts: List<Pair<String, String>>
    ): String? {
        val words = word
            .lowercase()
            .split(NOT_WORD)
            .filter {
                it.length >= MIN_WORD
            }

        if (words.isEmpty()) return null

        val lines = texts.flatMap { (note, text) ->
            linesOf(
                note = note,
                text = text,
                words = words
            )
        }

        if (lines.isEmpty()) return null

        val figures = lines.filter {
            it.figure != null
        }

        val shown = figures.ifEmpty { lines }.take(n = MOST_LINES)

        val listed = shown
            .joinToString(separator = "\n") { line ->
                val under = line
                    .heading
                    ?.let { ", under \"$it\"" }
                    .orEmpty()

                "- ${line.note}$under: ${line.text}"
            }

        val more = (figures.ifEmpty { lines }.size - shown.size)
            .takeIf {
                it > 0
            }
            ?.let { "\n(and $it more)" }
            .orEmpty()

        val total = figures
            .takeIf {
                it.size > 1
            }
            ?.let { found ->
                "\nThese ${found.size} figures add up to ${written(number = found.sumOf { it.figure ?: 0.0 })}."
            }
            .orEmpty()

        return "count: \"$word\" is written on ${figures.ifEmpty { lines }.size} lines in $place:\n" +
            listed + more + total
    }

    private fun linesOf(
        note: String,
        text: String,
        words: List<String>
    ): List<Line> {
        val headings = text
            .lines()
            .runningFold(initial = null as String?) { heading, line ->
                if (line.startsWith(prefix = HEADING)) line.trimStart('#', ' ') else heading
            }
            .drop(n = 1)

        return text
            .lines()
            .mapIndexedNotNull { index, line ->
                val folded = line.lowercase()

                val isWritten = !line.startsWith(prefix = HEADING) && words.all {
                    it in folded
                }

                if (isWritten) {
                    Line(
                        note = note,
                        heading = headings[index],
                        text = line.trim().take(n = LINE_CHARS),
                        figure = figureOf(
                            line = folded,
                            word = words.last()
                        )
                    )
                } else {
                    null
                }
            }
    }

    // The figure right after the word ("Spent: 6,400 yen", "points: 10"), or right before it ("10 points").
    private fun figureOf(
        line: String,
        word: String
    ): Double? {
        val after = Regex(pattern = "${Regex.escape(word)}\\w*[^\\p{L}\\p{N}]{1,4}($NUMBER)")
        val before = Regex(pattern = "($NUMBER)\\s+${Regex.escape(word)}")

        val written = after
            .find(input = line)
            ?.groupValues
            ?.get(1)
            ?: before
                .find(input = line)
                ?.groupValues
                ?.get(1)

        return written?.let { numberOf(written = it) }
    }

    // "6,400" and "6.400" are thousands; "12.5" and "12,5" a decimal.
    private fun numberOf(written: String): Double? {
        val isThousands = THOUSANDS.matches(input = written)

        val plain = if (isThousands) {
            written.filter { it.isDigit() }
        } else {
            written.replace(
                oldChar = ',',
                newChar = '.'
            )
        }

        return plain.toDoubleOrNull()
    }

    private fun written(number: Double): String =
        if (number % 1.0 == 0.0) number.toLong().toString() else number.toString()

    private class Line(
        val note: String,
        val text: String,
        val heading: String?,
        val figure: Double?
    )

    private const val HEADING = "#"
    private const val MIN_WORD = 3
    private const val MOST_LINES = 40
    private const val LINE_CHARS = 160
    private const val NUMBER = "\\d[\\d.,]*\\d|\\d"

    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")
    private val THOUSANDS = Regex(pattern = "\\d{1,3}([.,]\\d{3})+")
}
