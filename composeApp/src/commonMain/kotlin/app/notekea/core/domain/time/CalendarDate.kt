package app.notekea.core.domain.time

/**
 * A day of the proleptic Gregorian calendar, without a time or a time zone: what a daily note is named after
 * (`ROADMAP.md` 1.10). [month] runs from 1 to 12 and [day] from 1; [isValid] says whether the three make a real day.
 * Days are counted from 1970-01-01 ([epochDay]), so moving by days, weeks or years never trips over month lengths.
 */
data class CalendarDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<CalendarDate> {
    val isValid: Boolean
        get() = month in 1..MONTHS && day in 1..lengthOfMonth(year = year, month = month)

    /** Days since 1970-01-01 (negative before it), in Howard Hinnant's civil calendar arithmetic. */
    val epochDay: Long
        get() {
            val shiftedYear = (if (month <= 2) year - 1 else year).toLong()
            val era = (if (shiftedYear >= 0) shiftedYear else shiftedYear - ERA_YEARS + 1) / ERA_YEARS
            val yearOfEra = shiftedYear - era * ERA_YEARS
            val dayOfShiftedYear = (MONTH_LENGTH_SPAN * ((month + MARCH_SHIFT) % MONTHS) + 2) / MONTH_SPAN + day - 1
            val dayOfEra = yearOfEra * DAYS_IN_YEAR + yearOfEra / LEAP_CYCLE - yearOfEra / CENTURY + dayOfShiftedYear
            return era * DAYS_IN_ERA + dayOfEra - EPOCH_SHIFT
        }

    /** The ISO day of the week: 1 for Monday up to 7 for Sunday. */
    val dayOfWeek: Int
        get() = (epochDay + THURSDAY_OFFSET).mod(other = DAYS_IN_WEEK) + 1

    /** The day's place in its year, from 1 (January 1st). */
    val dayOfYear: Int
        get() = (epochDay - CalendarDate(year = year, month = 1, day = 1).epochDay).toInt() + 1

    /** The ISO week (1 to 53) and the year it belongs to, which differs from [year] around New Year. */
    val isoWeek: Pair<Int, Int>
        get() {
            val week = (dayOfYear - dayOfWeek + ISO_WEEK_SHIFT) / DAYS_IN_WEEK
            return when {
                week < 1 -> year - 1 to weeksIn(year = year - 1)
                week > weeksIn(year = year) -> year + 1 to 1
                else -> year to week
            }
        }

    fun plusDays(
        days: Long
    ): CalendarDate = ofEpochDay(epochDay = epochDay + days)

    override fun compareTo(
        other: CalendarDate
    ): Int = epochDay.compareTo(other = other.epochDay)

    companion object {
        const val MONTHS = 12
        const val DAYS_IN_WEEK = 7
        private const val CENTURY = 100
        private const val DAYS_IN_ERA = 146_097L
        private const val DAYS_IN_YEAR = 365
        private const val EPOCH_SHIFT = 719_468L
        private const val ERA_YEARS = 400L
        private const val FEBRUARY = 2
        private const val FEBRUARY_DAYS = 28
        private const val FEBRUARY_LEAP_DAYS = 29
        private const val ISO_WEEK_SHIFT = 10
        private const val LEAP_CYCLE = 4
        private const val LONG_MONTH = 31
        private const val MARCH_SHIFT = 9
        private const val MONTH_LENGTH_SPAN = 153
        private const val MONTH_SPAN = 5
        private const val SHORT_MONTH = 30
        private const val THURSDAY = 4
        private const val THURSDAY_OFFSET = 3
        private const val WEDNESDAY = 3
        private const val WEEKS_IN_LONG_YEAR = 53
        private const val WEEKS_IN_YEAR = 52
        private val SHORT_MONTHS = setOf(4, 6, 9, 11)

        /** The day [epochDay] days after 1970-01-01. */
        fun ofEpochDay(
            epochDay: Long
        ): CalendarDate {
            val shifted = epochDay + EPOCH_SHIFT
            val era = (if (shifted >= 0) shifted else shifted - DAYS_IN_ERA + 1) / DAYS_IN_ERA
            val dayOfEra = shifted - era * DAYS_IN_ERA
            val yearOfEra = (
                dayOfEra - dayOfEra / (DAYS_IN_YEAR * LEAP_CYCLE) + dayOfEra / (DAYS_IN_ERA / LEAP_CYCLE) -
                    dayOfEra / (DAYS_IN_ERA - 1)
                ) / DAYS_IN_YEAR
            val dayOfShiftedYear = dayOfEra - (DAYS_IN_YEAR * yearOfEra + yearOfEra / LEAP_CYCLE - yearOfEra / CENTURY)
            val shiftedMonth = (MONTH_SPAN * dayOfShiftedYear + 2) / MONTH_LENGTH_SPAN
            val day = dayOfShiftedYear - (MONTH_LENGTH_SPAN * shiftedMonth + 2) / MONTH_SPAN + 1
            val month = if (shiftedMonth < MONTHS - 2) shiftedMonth + 3 else shiftedMonth - MARCH_SHIFT
            val year = yearOfEra + era * ERA_YEARS + if (month <= FEBRUARY) 1 else 0
            return CalendarDate(year = year.toInt(), month = month.toInt(), day = day.toInt())
        }

        fun isLeapYear(
            year: Int
        ): Boolean = year % LEAP_CYCLE == 0 && (year % CENTURY != 0 || year % ERA_YEARS.toInt() == 0)

        fun lengthOfMonth(
            year: Int,
            month: Int
        ): Int = when {
            month == FEBRUARY -> if (isLeapYear(year = year)) FEBRUARY_LEAP_DAYS else FEBRUARY_DAYS
            month in SHORT_MONTHS -> SHORT_MONTH
            else -> LONG_MONTH
        }

        /** How many ISO weeks [year] has: 53 when it starts on a Thursday, or on a Wednesday in a leap year. */
        private fun weeksIn(
            year: Int
        ): Int {
            val firstDay = CalendarDate(year = year, month = 1, day = 1).dayOfWeek
            val isLong = firstDay == THURSDAY || (firstDay == WEDNESDAY && isLeapYear(year = year))
            return if (isLong) WEEKS_IN_LONG_YEAR else WEEKS_IN_YEAR
        }
    }
}
