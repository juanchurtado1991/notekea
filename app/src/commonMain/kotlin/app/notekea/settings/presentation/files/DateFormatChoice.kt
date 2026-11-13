package app.notekea.settings.presentation.files

import app.notekea.core.domain.time.CalendarDate

/**
 * A date format a daily note can be named in, shown by what today looks like in it ([example], "13-11-2026") so nobody
 * has to read "DD-MM-YYYY" (the developer's request of 2026-11-13: the field was not understood).
 */
data class DateFormatChoice(
    val format: String,
    val example: String
) {
    companion object {
        /** The formats offered, the first being Obsidian's own. */
        val FORMATS =
            listOf("YYYY-MM-DD", "DD-MM-YYYY", "MM-DD-YYYY", "YYYY.MM.DD", "DD.MM.YYYY", "YYYYMMDD")
        private const val TWO = 2
        private const val FOUR = 4

        /** [FORMATS] as [today] is written in each, and [current] too when it is none of them (kept as it is). */
        fun choicesOf(
            today: CalendarDate,
            current: String
        ): List<DateFormatChoice> = (FORMATS + current)
            .filter { it.isNotBlank() }
            .distinct()
            .map { format ->
                val isKnown = format in FORMATS

                val example = if (isKnown) {
                    written(
                        format = format,
                        date = today
                    )
                } else {
                    format
                }

                DateFormatChoice(
                    format = format,
                    example = example
                )
            }

        private fun written(
            format: String,
            date: CalendarDate
        ): String = format
            .replace(
                oldValue = "YYYY",
                newValue = date
                    .year
                    .toString()
                    .padStart(
                        length = FOUR,
                        padChar = '0'
                    )
            )
            .replace(
                oldValue = "MM",
                newValue = date
                    .month
                    .toString()
                    .padStart(
                        length = TWO,
                        padChar = '0'
                    )
            )
            .replace(
                oldValue = "DD",
                newValue = date
                    .day
                    .toString()
                    .padStart(
                        length = TWO,
                        padChar = '0'
                    )
            )
    }
}
