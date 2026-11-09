package app.notekea.models.data

import app.notekea.core.domain.FakeClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/** The speed shown next to a download (smoke of 3.1): measured over a second or more, as browsers do. */
class DownloadSpeedMeterTest {
    private val clock = FakeClock(currentTime = Instant.fromEpochMilliseconds(epochMilliseconds = 0))
    private val meter = DownloadSpeedMeter(clock = clock)

    private fun after(
        millis: Int,
        doneBytes: Long
    ): Long? {
        clock.currentTime += millis.milliseconds
        return meter.measure(doneBytes = doneBytes)
    }

    @Test
    fun `no speed before a second, then the bytes of the last second or more`() {
        val speeds = listOf(
            after(millis = 0, doneBytes = 0),
            after(millis = 400, doneBytes = 8_000_000),
            after(millis = 600, doneBytes = 20_000_000),
            after(millis = 500, doneBytes = 30_000_000),
            after(millis = 1_500, doneBytes = 50_000_000)
        )

        assertEquals(expected = listOf(null, null, 20_000_000L, 20_000_000L, 15_000_000L), actual = speeds)
    }

    @Test
    fun `a download that starts over measures again from there`() {
        after(millis = 0, doneBytes = 0)
        after(millis = 1_000, doneBytes = 10_000_000)

        val speeds = listOf(after(millis = 100, doneBytes = 0), after(millis = 2_000, doneBytes = 4_000_000))

        assertEquals(expected = listOf(10_000_000L, 2_000_000L), actual = speeds)
    }
}
