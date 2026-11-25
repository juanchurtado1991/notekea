package app.notekea.chat.domain.plan

import app.notekea.chat.domain.tool.app.FigureValues

/**
 * The figures of a text written from notes that the notes do not hold, checked as its last step: a price made up reads
 * as true as one copied. One-digit numbers (counts, list numbers) and what the copied ones add up to ([WorkedFigures])
 * are left out: a right total was taken for a made-up one and written out of the note (dev, 2026-11-25).
 */
internal object BackedFigures {
    private val FIGURE = Regex(pattern = "\\d[\\d.,]*")
    private const val MIN_DIGITS = 2

    fun missingIn(
        text: String,
        sources: List<String>
    ): List<String> {
        val held = sources.flatMap { FigureValues.allIn(text = it) }

        return text
            .lines()
            .flatMap { line ->
                FIGURE
                    .findAll(input = line)
                    .map { it.value.trimEnd('.', ',') }
                    .filter { figure ->
                        figure.count { it.isDigit() } >= MIN_DIGITS
                    }
                    .filterNot { isBacked(it, line, text, sources, held) }
            }
            .distinct()
    }

    // A mark that is no value ("12.03.2026", a version) is backed where a note writes it so.
    private fun isBacked(
        figure: String,
        line: String,
        text: String,
        sources: List<String>,
        held: List<Double>
    ): Boolean {
        val value = FigureValues.valueOf(written = figure)
            ?: return sources.any {
                figure in it
            }

        return WorkedFigures.isAmong(
            value = value,
            values = held
        ) || WorkedFigures.isWorked(
            value = value,
            line = line,
            text = text,
            held = held
        )
    }
}
