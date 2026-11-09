package app.notekea.ai.data

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The speed of one download, as browsers show it: the bytes that arrived over the last second or more, so a short
 * burst does not make the number jump. Null until the first second has passed.
 */
class DownloadSpeedMeter(
    private val clock: Clock
) {
    private var windowStart: Instant? = null
    private var windowBytes = 0L
    private var speed: Long? = null

    fun measure(
        doneBytes: Long
    ): Long? {
        val now = clock.now()
        val start = windowStart
        if (start == null || doneBytes < windowBytes) {
            windowStart = now
            windowBytes = doneBytes
            return speed
        }
        val elapsed = now - start
        if (elapsed.inWholeMilliseconds >= WINDOW_MILLIS) {
            speed = (doneBytes - windowBytes) * MILLIS_PER_SECOND / elapsed.inWholeMilliseconds
            windowStart = now
            windowBytes = doneBytes
        }
        return speed
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
        const val WINDOW_MILLIS = 1_000L
    }
}
