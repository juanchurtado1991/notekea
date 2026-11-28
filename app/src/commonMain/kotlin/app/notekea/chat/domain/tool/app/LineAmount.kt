package app.notekea.chat.domain.tool.app

import app.notekea.core.domain.figures.FigureValues

/**
 * The amount a tally item counts, as the model writes it: a date or a time is no amount, a figure with its
 * currency is the line's amount, else its last figure. The E2B's blind pool, seed 10, 2026-11-28: the row
 * "| 2026-08-05 | Cascade Pacific | gas | $38 |" was counted as 2026, and "Cuota 1 (anteproyecto): $3.500.000" as 1.
 */
internal object LineAmount {
    fun of(text: String): Double? {
        val plain = text
            .replace(
                regex = DATE,
                replacement = " "
            )
            .replace(
                regex = TIME,
                replacement = " "
            )

        val priced = PRICED
            .find(input = plain)
            ?.let {
                it.groupValues[1].ifEmpty {
                    it.groupValues[2]
                }
            }
            ?.let { FigureValues.valueOf(written = it) }

        return priced ?: FigureValues.allIn(text = plain).lastOrNull()
    }

    private val DATE = Regex(pattern = "\\d{4}-\\d{2}-\\d{2}")
    private val TIME = Regex(pattern = "\\b\\d{1,2}:\\d{2}\\b")
    private val PRICED = Regex(pattern = "[$€£]\\s?(\\d[\\d.,]*)|(\\d[\\d.,]*)\\s?[$€£]")
}
