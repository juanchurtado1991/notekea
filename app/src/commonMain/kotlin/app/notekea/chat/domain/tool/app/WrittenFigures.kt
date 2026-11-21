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
        texts: List<Pair<String, String>>,
        task: String = ""
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

        val part = partOf(
            figures = figures,
            task = task,
            words = words
        )

        return "count: \"$word\" is written on ${figures.ifEmpty { lines }.size} lines in $place:\n" +
            listed + more + total + part
    }

    // The lines a word of the task picks out, added apart: "how much did I spend in Kyoto" was answered with every
    // day's spending (T5, 211.5 for 210). The first word of the task some figure lines write and some do not.
    private fun partOf(
        figures: List<Line>,
        task: String,
        words: List<String>
    ): String {
        val asked = task
            .lowercase()
            .split(NOT_WORD)
            .filter {
                it.length >= MIN_TASK_WORD &&
                    it !in words &&
                    it !in COMMON
            }

        val (word, picked) = asked
            .asSequence()
            .map { candidate ->
                candidate to figures.filter {
                    val under = it
                        .heading
                        ?.lowercase()
                        .orEmpty()

                    candidate in it.text.lowercase() || candidate in under
                }
            }
            .firstOrNull { (_, picked) ->
                picked.isNotEmpty() && picked.size < figures.size
            } ?: return ""

        val rest = figures - picked.toSet()

        return "\nOf them, the ${picked.size} that write \"$word\" add up to " +
            "${written(number = picked.sumOf { it.figure ?: 0.0 })}, and the other ${rest.size} to " +
            "${written(number = rest.sumOf { it.figure ?: 0.0 })}: count only those the task asks for."
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

    // The figure right before the word ("10 points"), else right after it ("Spent: 6,400 yen", "points: 10"):
    // "Homework 1 (10 points), 27 August" read 27, the number after the word, for its 10 (T6).
    private fun figureOf(
        line: String,
        word: String
    ): Double? {
        val after = Regex(pattern = "${Regex.escape(word)}\\w*[^\\p{L}\\p{N}]{1,4}($NUMBER)")
        val before = Regex(pattern = "($NUMBER)\\s+${Regex.escape(word)}")

        val written = before
            .find(input = line)
            ?.groupValues
            ?.get(1)
            ?: after
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
    private const val MIN_TASK_WORD = 4
    private const val MOST_LINES = 40
    private const val LINE_CHARS = 160
    private const val NUMBER = "\\d[\\d.,]*\\d|\\d"

    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")

    // Words of a question that pick out no lines: "how much did I spend in total".
    private val COMMON = setOf(
        "what", "much", "many", "does", "have", "total", "with", "from", "that", "this", "these",
        "those", "were", "there", "they", "their", "notes", "note", "adds", "count", "spend",
        "spent", "points", "cuánto", "cuanto", "cuántos", "cuantos", "cuántas", "cuantas", "suma",
        "nota", "notas", "todo", "todos", "todas", "gasté", "gaste", "tengo", "para", "desde",
        "hasta", "sobre", "entre"
    )
    private val THOUSANDS = Regex(pattern = "\\d{1,3}([.,]\\d{3})+")
}
