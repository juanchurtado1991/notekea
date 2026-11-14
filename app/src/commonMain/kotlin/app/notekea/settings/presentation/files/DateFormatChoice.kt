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
        private const val MONTHS = 12
        private const val SAMPLE_DAY = 25
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

                // A day over 12 tells "25-11-2026" from "11-25-2026"; 03-04 and 04-03 read alike.
                val sample = if (today.day > MONTHS) today else today.copy(day = SAMPLE_DAY)

                val example = if (isKnown) {
                    written(
                        format = format,
                        date = sample
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
