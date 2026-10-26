package app.notekea.core.domain.time

/**
 * Dates written and read with the format strings of Moment.js, which Obsidian uses for daily note names and template
 * variables (`YYYY-MM-DD`, `dddd, MMMM Do YYYY`, `YYYY/MM/YYYY-MM-DD`), with English month and day names as Obsidian
 * has by default. Text in `[brackets]` is written as it is; any other character that is not a token too. Supported
 * tokens: years `YYYY` `YY`, quarter `Q`, months `MMMM` `MMM` `MM` `M`, days `DD` `D` `Do`, day of the year `DDDD`
 * `DDD`, weekdays `dddd` `ddd` `dd` `d`, ISO weeks `WW` `W` and their year `GGGG`, and the time `HH` `H` `hh` `h` `mm`
 * `m` `ss` `s` `A` `a`.
 */
class MomentFormat {
    // Daily notes read every note name with the same pattern: its tokens and regular expression are built once.
    private val readers = mutableMapOf<String, Pair<List<Token>, Regex>>()

    fun format(
        moment: LocalMoment,
        pattern: String
    ): String = tokensOf(pattern = pattern).joinToString(separator = "") { token ->
        if (token.isLiteral) token.text else write(token = token.text, moment = moment)
    }

    /**
     * The date [text] names in [pattern], or null when it does not name one: a real day whose name, written again
     * with [pattern], is exactly [text] (so `2026-10-5` is not `YYYY-MM-DD`, and a wrong weekday does not match).
     */
    fun parseDate(
        text: String,
        pattern: String
    ): CalendarDate? {
        val (tokens, regex) = readers.getOrPut(key = pattern) { readerOf(pattern = pattern) }
        val values = regex.matchEntire(input = text)?.groupValues?.drop(n = 1) ?: return null
        val fields = tokens.filterNot { it.isLiteral || readPatternOf(token = it.text).startsWith(NON_CAPTURING) }
            .map { it.text }
            .zip(other = values)
            .toMap()
        val date = dateOf(fields = fields) ?: return null
        return date.takeIf { it.isValid && format(moment = LocalMoment(date = it), pattern = pattern) == text }
    }

    private fun readerOf(
        pattern: String
    ): Pair<List<Token>, Regex> {
        val tokens = tokensOf(pattern = pattern)
        val regex = Regex(
            pattern = tokens.joinToString(separator = "", prefix = "^", postfix = "$") { token ->
                if (token.isLiteral) Regex.escape(literal = token.text) else readPatternOf(token = token.text)
            }
        )
        return tokens to regex
    }

    private fun dateOf(
        fields: Map<String, String>
    ): CalendarDate? {
        val year = fields[YEAR]?.toIntOrNull()
            ?: fields[SHORT_YEAR]?.toIntOrNull()?.let { CENTURY_START + it }
            ?: return null
        val dayOfYear = (fields[DAY_OF_YEAR_PADDED] ?: fields[DAY_OF_YEAR])?.toIntOrNull()
        val month = (fields[MONTH_PADDED] ?: fields[MONTH])?.toIntOrNull()
            ?: fields[MONTH_NAME]?.let { MONTH_NAMES.indexOf(element = it) + 1 }
            ?: fields[MONTH_SHORT_NAME]?.let { name -> MONTH_NAMES.indexOfFirst { it.startsWith(prefix = name) } + 1 }
        val day = (fields[DAY_PADDED] ?: fields[DAY] ?: fields[DAY_ORDINAL])?.toIntOrNull()
        return when {
            month != null && day != null -> CalendarDate(year = year, month = month, day = day)
            dayOfYear != null -> CalendarDate(year = year, month = 1, day = 1).plusDays(days = dayOfYear - 1L)
            else -> null
        }
    }

    private fun write(
        token: String,
        moment: LocalMoment
    ): String {
        val date = moment.date
        return when (token) {
            YEAR -> date.year.toString().padStart(length = YEAR_DIGITS, padChar = ZERO)
            SHORT_YEAR -> two(value = date.year.mod(other = CENTURY))
            QUARTER -> ((date.month - 1) / MONTHS_IN_QUARTER + 1).toString()
            MONTH_NAME -> MONTH_NAMES[date.month - 1]
            MONTH_SHORT_NAME -> MONTH_NAMES[date.month - 1].take(n = SHORT_NAME_LENGTH)
            MONTH_PADDED -> two(value = date.month)
            MONTH -> date.month.toString()
            DAY_OF_YEAR_PADDED -> date.dayOfYear.toString().padStart(length = DAY_OF_YEAR_DIGITS, padChar = ZERO)
            DAY_OF_YEAR -> date.dayOfYear.toString()
            DAY_ORDINAL -> date.day.toString() + ordinalSuffix(day = date.day)
            DAY_PADDED -> two(value = date.day)
            DAY -> date.day.toString()
            else -> writeWeekOrTime(token = token, moment = moment)
        }
    }

    private fun writeWeekOrTime(
        token: String,
        moment: LocalMoment
    ): String {
        val (weekYear, week) = moment.date.isoWeek
        val weekday = DAY_NAMES[moment.date.dayOfWeek - 1]
        val twelveHour = (moment.hour + HOURS_IN_HALF_DAY - 1) % HOURS_IN_HALF_DAY + 1
        return when (token) {
            WEEKDAY_NAME -> weekday
            WEEKDAY_SHORT_NAME -> weekday.take(n = SHORT_NAME_LENGTH)
            WEEKDAY_MIN_NAME -> weekday.take(n = MIN_NAME_LENGTH)
            WEEKDAY_NUMBER -> (moment.date.dayOfWeek % CalendarDate.DAYS_IN_WEEK).toString()
            ISO_WEEK_YEAR -> weekYear.toString()
            ISO_WEEK_PADDED -> two(value = week)
            ISO_WEEK -> week.toString()
            HOUR_PADDED -> two(value = moment.hour)
            HOUR -> moment.hour.toString()
            TWELVE_HOUR_PADDED -> two(value = twelveHour)
            TWELVE_HOUR -> twelveHour.toString()
            MINUTE_PADDED -> two(value = moment.minute)
            MINUTE -> moment.minute.toString()
            SECOND_PADDED -> two(value = moment.second)
            SECOND -> moment.second.toString()
            MERIDIEM_UPPER -> if (moment.hour < HOURS_IN_HALF_DAY) AM else PM
            else -> (if (moment.hour < HOURS_IN_HALF_DAY) AM else PM).lowercase()
        }
    }

    /** What a token reads: a captured group for the fields that name the date, a non-capturing one for the rest. */
    private fun readPatternOf(
        token: String
    ): String = when (token) {
        YEAR -> "(\\d{4})"

        SHORT_YEAR, MONTH_PADDED, DAY_PADDED -> "(\\d{2})"

        MONTH, DAY -> "(\\d{1,2})"

        MONTH_NAME -> MONTH_NAMES.joinToString(separator = "|", prefix = "(", postfix = ")")

        MONTH_SHORT_NAME -> MONTH_NAMES.joinToString(separator = "|", prefix = "(", postfix = ")") {
            it.take(n = SHORT_NAME_LENGTH)
        }

        DAY_ORDINAL -> "(\\d{1,2})(?:st|nd|rd|th)"

        DAY_OF_YEAR_PADDED -> "(\\d{3})"

        DAY_OF_YEAR -> "(\\d{1,3})"

        else -> "$NON_CAPTURING.+?)"
    }

    private fun ordinalSuffix(
        day: Int
    ): String = when {
        day % CENTURY in TEENS -> TH
        day % TEN == 1 -> ST
        day % TEN == 2 -> ND
        day % TEN == THREE -> RD
        else -> TH
    }

    private fun two(
        value: Int
    ): String = value.toString().padStart(length = 2, padChar = ZERO)

    /** [pattern] cut into tokens and literal text, the longest token first where several start alike. */
    private fun tokensOf(
        pattern: String
    ): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        while (index < pattern.length) {
            if (pattern[index] == ESCAPE_OPEN) {
                val end = pattern.indexOf(char = ESCAPE_CLOSE, startIndex = index).takeIf { it > 0 } ?: pattern.length
                tokens += Token(text = pattern.substring(startIndex = index + 1, endIndex = end), isLiteral = true)
                index = end + 1
                continue
            }
            val token = TOKENS.firstOrNull { pattern.startsWith(prefix = it, startIndex = index) }
            tokens += Token(text = token ?: pattern[index].toString(), isLiteral = token == null)
            index += token?.length ?: 1
        }
        return tokens
    }

    /** A piece of a pattern: a token to replace, or [isLiteral] text written as it is. */
    private data class Token(
        val text: String,
        val isLiteral: Boolean
    )

    companion object {
        /** Obsidian's default daily note format. */
        const val ISO_DATE = "YYYY-MM-DD"

        /** The format of `{{time}}` in Obsidian's templates. */
        const val SHORT_TIME = "HH:mm"
        private const val AM = "AM"
        private const val CENTURY = 100
        private const val CENTURY_START = 2000
        private const val DAY = "D"
        private const val DAY_OF_YEAR = "DDD"
        private const val DAY_OF_YEAR_DIGITS = 3
        private const val DAY_OF_YEAR_PADDED = "DDDD"
        private const val DAY_ORDINAL = "Do"
        private const val DAY_PADDED = "DD"
        private const val ESCAPE_CLOSE = ']'
        private const val ESCAPE_OPEN = '['
        private const val HOUR = "H"
        private const val HOUR_PADDED = "HH"
        private const val HOURS_IN_HALF_DAY = 12
        private const val ISO_WEEK = "W"
        private const val ISO_WEEK_PADDED = "WW"
        private const val ISO_WEEK_YEAR = "GGGG"
        private const val MERIDIEM_LOWER = "a"
        private const val MERIDIEM_UPPER = "A"
        private const val MIN_NAME_LENGTH = 2
        private const val MINUTE = "m"
        private const val MINUTE_PADDED = "mm"
        private const val MONTH = "M"
        private const val MONTH_NAME = "MMMM"
        private const val MONTH_PADDED = "MM"
        private const val MONTH_SHORT_NAME = "MMM"
        private const val MONTHS_IN_QUARTER = 3
        private const val ND = "nd"
        private const val NON_CAPTURING = "(?:"
        private const val PM = "PM"
        private const val QUARTER = "Q"
        private const val RD = "rd"
        private const val SECOND = "s"
        private const val SECOND_PADDED = "ss"
        private const val SHORT_NAME_LENGTH = 3
        private const val SHORT_YEAR = "YY"
        private const val ST = "st"
        private const val TEN = 10
        private const val TH = "th"
        private const val THREE = 3
        private const val TWELVE_HOUR = "h"
        private const val TWELVE_HOUR_PADDED = "hh"
        private const val WEEKDAY_MIN_NAME = "dd"
        private const val WEEKDAY_NAME = "dddd"
        private const val WEEKDAY_NUMBER = "d"
        private const val WEEKDAY_SHORT_NAME = "ddd"
        private const val YEAR = "YYYY"
        private const val YEAR_DIGITS = 4
        private const val ZERO = '0'
        private val TEENS = 11..13
        private val DAY_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        private val MONTH_NAMES = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        private val TOKENS = listOf(
            YEAR, SHORT_YEAR, QUARTER, MONTH_NAME, MONTH_SHORT_NAME, MONTH_PADDED, MONTH, DAY_OF_YEAR_PADDED,
            DAY_OF_YEAR, DAY_ORDINAL, DAY_PADDED, DAY, WEEKDAY_NAME, WEEKDAY_SHORT_NAME, WEEKDAY_MIN_NAME,
            WEEKDAY_NUMBER, ISO_WEEK_YEAR, ISO_WEEK_PADDED, ISO_WEEK, HOUR_PADDED, HOUR, TWELVE_HOUR_PADDED,
            TWELVE_HOUR, MINUTE_PADDED, MINUTE, SECOND_PADDED, SECOND, MERIDIEM_UPPER, MERIDIEM_LOWER
        )
    }
}
