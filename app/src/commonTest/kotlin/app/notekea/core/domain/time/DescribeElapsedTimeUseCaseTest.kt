package app.notekea.core.domain.time

import app.notekea.core.domain.FakeClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class DescribeElapsedTimeUseCaseTest {
    private val now = Instant.fromEpochMilliseconds(epochMilliseconds = NOW_MILLIS)
    private val describeElapsedTime = DescribeElapsedTimeUseCase(clock = FakeClock(currentTime = now))

    @Test
    fun `each range uses the largest whole unit`() {
        val cases = mapOf(
            30.seconds to ElapsedTime.JustNow,
            5.minutes to ElapsedTime.Minutes(count = 5),
            59.minutes to ElapsedTime.Minutes(count = 59),
            3.hours to ElapsedTime.Hours(count = 3),
            1.days to ElapsedTime.Days(count = 1),
            6.days to ElapsedTime.Days(count = 6),
            7.days to ElapsedTime.Weeks(count = 1),
            29.days to ElapsedTime.Weeks(count = 4),
            30.days to ElapsedTime.Months(count = 1),
            364.days to ElapsedTime.Months(count = 12),
            365.days to ElapsedTime.Years(count = 1),
            800.days to ElapsedTime.Years(count = 2)
        )

        cases.forEach { (elapsed, expected) ->
            assertEquals(
                expected = expected,
                actual = describe(elapsed = elapsed),
                message = elapsed.toString()
            )
        }
    }

    @Test
    fun `a time in the future counts as just now`() {
        assertEquals(
            expected = ElapsedTime.JustNow,
            actual = describe(elapsed = -(2.hours))
        )
    }

    private fun describe(
        elapsed: Duration
    ): ElapsedTime = describeElapsedTime(since = now - elapsed)

    companion object {
        private const val NOW_MILLIS = 1_800_000_000_000L
    }
}
