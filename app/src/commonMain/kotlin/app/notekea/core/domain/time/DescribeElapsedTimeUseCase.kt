package app.notekea.core.domain.time

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Turns a past [Instant] into an [ElapsedTime] relative to the injected clock. Times in the future (a clock that
 * moved backwards) count as "just now" instead of producing negative values.
 */
class DescribeElapsedTimeUseCase(private val clock: Clock) {
    operator fun invoke(
        since: Instant
    ): ElapsedTime {
        val elapsed = clock.now() - since
        return when {
            elapsed < 1.minutes -> ElapsedTime.JustNow
            elapsed < 1.hours -> ElapsedTime.Minutes(count = elapsed.inWholeMinutes.toInt())
            elapsed < 1.days -> ElapsedTime.Hours(count = elapsed.inWholeHours.toInt())
            elapsed < WEEK -> ElapsedTime.Days(count = elapsed.inWholeDays.toInt())
            elapsed < MONTH -> ElapsedTime.Weeks(count = elapsed.wholeUnits(unit = WEEK))
            elapsed < YEAR -> ElapsedTime.Months(count = elapsed.wholeUnits(unit = MONTH))
            else -> ElapsedTime.Years(count = elapsed.wholeUnits(unit = YEAR))
        }
    }

    private fun Duration.wholeUnits(
        unit: Duration
    ): Int = (this / unit).toInt()

    companion object {
        private const val DAYS_PER_MONTH = 30
        private const val DAYS_PER_WEEK = 7
        private const val DAYS_PER_YEAR = 365
        private val MONTH = DAYS_PER_MONTH.days
        private val WEEK = DAYS_PER_WEEK.days
        private val YEAR = DAYS_PER_YEAR.days
    }
}
