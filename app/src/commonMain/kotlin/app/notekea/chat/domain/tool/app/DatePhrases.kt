package app.notekea.chat.domain.tool.app

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.TextFolder

/**
 * The day a phrase names, counted from today by the app: "tomorrow", "next Friday", "in 3 weeks", "end of month",
 * in English or Spanish. The model gets weekdays and month lengths wrong; the calendar does not.
 */
internal object DatePhrases {
    private const val WEEK = 7L
    private val ISO = Regex(pattern = "(\\d{4})-(\\d{1,2})-(\\d{1,2})")
    private val AHEAD = Regex(pattern = "(?:in|en|dentro de) (\\d+) ([a-z]+)")
    private val BACK = Regex(pattern = "(\\d+) ([a-z]+) ago|hace (\\d+) ([a-z]+)")
    private val WEEKDAYS = listOf(
        listOf("monday", "lunes"),
        listOf("tuesday", "martes"),
        listOf("wednesday", "miercoles"),
        listOf("thursday", "jueves"),
        listOf("friday", "viernes"),
        listOf("saturday", "sabado"),
        listOf("sunday", "domingo")
    )

    private val MONTHS = listOf(
        listOf("january", "enero"),
        listOf("february", "febrero"),
        listOf("march", "marzo"),
        listOf("april", "abril"),
        listOf("may", "mayo"),
        listOf("june", "junio"),
        listOf("july", "julio"),
        listOf("august", "agosto"),
        listOf("september", "septiembre", "setiembre"),
        listOf("october", "octubre"),
        listOf("november", "noviembre"),
        listOf("december", "diciembre")
    )

    // "15 de noviembre", "15 November 2027", "November 15th".
    private val DAY_MONTH =
        Regex(pattern = "\\b(\\d{1,2}) (?:de )?([a-z]+)(?:,? (?:de |del )?(\\d{4}))?")
    private val MONTH_DAY =
        Regex(pattern = "\\b([a-z]+) (\\d{1,2})(?:st|nd|rd|th)?\\b(?:,? (\\d{4}))?")

    private val NAMES =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    private val FIXED = listOf(
        listOf("day after tomorrow", "pasado manana") to 2L,
        listOf("tomorrow", "manana") to 1L,
        listOf("yesterday", "ayer") to -1L,
        listOf("today", "hoy") to 0L,
        listOf("next week", "proxima semana", "semana que viene") to WEEK
    )

    /**
     * The day [phrase] names, counted from [today]; null when it names none this reads. A day written whole
     * (2026-12-06) is read with no [today].
     */
    fun of(
        phrase: String,
        today: CalendarDate?
    ): CalendarDate? {
        val said = TextFolder.fold(text = phrase)

        return offset(said = said)
            ?: written(said = said)
            ?: today?.let {
                counted(
                    said = said,
                    today = it
                )
            }
    }

    private fun counted(
        said: String,
        today: CalendarDate
    ): CalendarDate? {
        val fixed = FIXED.firstOrNull { (words, _) ->
            words.any {
                it in said
            }
        }

        val weekday = WEEKDAYS.indexOfFirst { names ->
            names.any {
                it in said
            }
        }

        val day = named(
            said = said,
            today = today
        )

        return day
            ?: inWeek(
                said = said,
                weekday = weekday + 1,
                today = today
            )
            ?: ended(
                said = said,
                today = today
            )
            ?: moved(
                said = said,
                today = today
            )
            ?: fixed?.let { today.plusDays(days = it.second) }
            ?: weekday.takeIf {
                it >= 0
            }?.let {
                coming(
                    weekday = it + 1,
                    today = today
                )
            }
    }

    /** [date] as `2026-11-20 (Friday)`. */
    fun written(date: CalendarDate): String = "${date.iso} (${NAMES[date.dayOfWeek - 1]})"

    // A span before or after a written day: "una semana antes de 2026-04-03" was answered 2026-04-03 itself (the
    // loop's eighth run), as a written day was taken alone.
    private fun offset(said: String): CalendarDate? {
        val from = written(said = said) ?: return null
        val span = OFFSET.find(input = said)?.groupValues ?: return null
        val count = span[1].toLongOrNull() ?: COUNTS[span[1]] ?: 1L
        val isBefore = span[3].startsWith(prefix = "before") || span[3].startsWith(prefix = "antes")

        return shifted(
            today = from,
            count = if (isBefore) -count else count,
            unit = span[2]
        )
    }

    private fun written(said: String): CalendarDate? = ISO.find(input = said)?.let { match ->
        CalendarDate(
            year = match.groupValues[1].toInt(),
            month = match.groupValues[2].toInt(),
            day = match.groupValues[3].toInt()
        ).takeIf { it.isValid }
    }

    // A day and a month's name, in this year when none is written: the date tool could not say "el 15 de noviembre"
    // (the loop's fifth run, both models).
    private fun named(
        said: String,
        today: CalendarDate
    ): CalendarDate? {
        val dayFirst = DAY_MONTH
            .findAll(input = said)
            .map { it.groupValues }
            .map { Triple(it[1], it[2], it[3]) }

        val monthFirst = MONTH_DAY
            .findAll(input = said)
            .map { it.groupValues }
            .map { Triple(it[2], it[1], it[3]) }

        return (dayFirst + monthFirst).firstNotNullOfOrNull { (day, month, year) ->
            val index = MONTHS.indexOfFirst {
                month in it
            }

            if (index < 0) return@firstNotNullOfOrNull null

            CalendarDate(
                year = year.toIntOrNull() ?: today.year,
                month = index + 1,
                day = day.toInt()
            ).takeIf { it.isValid }
        }
    }

    // "in 3 weeks", "hace 2 meses", "next month".
    private fun moved(
        said: String,
        today: CalendarDate
    ): CalendarDate? {
        val ahead = AHEAD.find(input = said)?.groupValues

        val back = BACK
            .find(input = said)
            ?.groupValues
            ?.filter { it.isNotEmpty() }

        val isNextMonth =
            "next month" in said ||
                "proximo mes" in said ||
                "mes que viene" in said

        val isNextYear =
            "next year" in said ||
                "proximo ano" in said ||
                "ano que viene" in said

        return when {
            ahead != null -> shifted(
                today = today,
                count = ahead[1].toLong(),
                unit = ahead[2]
            )

            back != null -> shifted(
                today = today,
                count = -back[1].toLong(),
                unit = back[2]
            )

            isNextMonth -> plusMonths(
                date = today,
                months = 1
            )

            isNextYear -> plusMonths(
                date = today,
                months = CalendarDate.MONTHS.toLong()
            )

            else -> null
        }
    }

    private fun shifted(
        today: CalendarDate,
        count: Long,
        unit: String
    ): CalendarDate? = when {
        unit.startsWith(prefix = "day") || unit.startsWith(prefix = "dia") ->
            today.plusDays(days = count)

        unit.startsWith(prefix = "week") || unit.startsWith(prefix = "semana") ->
            today.plusDays(days = count * CalendarDate.DAYS_IN_WEEK)

        unit.startsWith(prefix = "month") || unit.startsWith(prefix = "mes") ->
            plusMonths(
                date = today,
                months = count
            )

        unit.startsWith(prefix = "year") || unit.startsWith(prefix = "ano") ->
            plusMonths(
                date = today,
                months = count * CalendarDate.MONTHS
            )

        else -> null
    }

    // A weekday of next week, or the last one gone: "friday next week" was the day a week on, and "last friday"
    // the coming one (T11). [weekday] is 1 for Monday, 0 for none.
    private fun inWeek(
        said: String,
        weekday: Int,
        today: CalendarDate
    ): CalendarDate? {
        if (weekday < 1) return null

        val isNextWeek = NEXT_WEEK.any {
            it in said
        }

        val isLast = LAST.containsMatchIn(input = said)

        return when {
            isNextWeek -> {
                val monday = today.plusDays(days = 1L - today.dayOfWeek + WEEK)

                monday.plusDays(days = weekday - 1L)
            }

            isLast -> {
                val back = (today.dayOfWeek - weekday).mod(other = CalendarDate.DAYS_IN_WEEK)

                today.plusDays(days = -(if (back == 0) WEEK else back.toLong()))
            }

            else -> null
        }
    }

    // "end of next month" is the last day of next month, not this day of it (T11).
    private fun ended(
        said: String,
        today: CalendarDate
    ): CalendarDate? {
        val isEnd =
            "end of" in said ||
                "fin de" in said ||
                "final de" in said

        val isYear = "year" in said || " ano" in said
        val isMonth = "month" in said || "mes" in said
        val isNext = NEXT.containsMatchIn(input = said)

        val ahead = when {
            !isNext -> 0L
            isYear -> CalendarDate.MONTHS.toLong()
            else -> 1L
        }

        val base = plusMonths(
            date = today,
            months = ahead
        )

        return when {
            !isEnd -> null

            isYear -> base.copy(
                month = CalendarDate.MONTHS,
                day = LAST_DAY_OF_YEAR
            )

            isMonth -> base.copy(
                day = CalendarDate.lengthOfMonth(
                    year = base.year,
                    month = base.month
                )
            )

            else -> null
        }
    }

    // The next day that is [weekday] (1 for Monday), never today itself.
    private fun coming(
        weekday: Int,
        today: CalendarDate
    ): CalendarDate {
        val ahead = (weekday - today.dayOfWeek).mod(other = CalendarDate.DAYS_IN_WEEK)

        val days = if (ahead == 0) WEEK else ahead.toLong()

        return today.plusDays(days = days)
    }

    // The same day [months] later, or the month's last day when it has no such day (31 January, a month on).
    private fun plusMonths(
        date: CalendarDate,
        months: Long
    ): CalendarDate {
        val index = date.year * CalendarDate.MONTHS + (date.month - 1) + months
        val year = index.floorDiv(other = CalendarDate.MONTHS).toInt()
        val month = index.mod(other = CalendarDate.MONTHS) + 1

        val length = CalendarDate.lengthOfMonth(
            year = year,
            month = month
        )

        return CalendarDate(
            year = year,
            month = month,
            day = date.day.coerceAtMost(maximumValue = length)
        )
    }

    private const val LAST_DAY_OF_YEAR = 31

    private val NEXT_WEEK = listOf("next week", "proxima semana", "semana que viene")
    private val LAST = Regex(pattern = "\\b(last|past|previous|pasado|anterior)\\b")
    private val NEXT = Regex(pattern = "\\b(next|proximo|que viene)\\b")

    private val OFFSET = Regex(
        pattern = "(\\d+|an?|one|two|three|un|una|dos|tres)\\s+(days?|weeks?|months?|dias?|semanas?|mes|meses)" +
            "\\s+(before|after|antes|despues)"
    )

    private val COUNTS = mapOf(
        "two" to 2L,
        "dos" to 2L,
        "three" to 3L,
        "tres" to 3L
    )
}
