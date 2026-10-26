package app.notekea.core.domain

import kotlin.time.Instant

/** The local time in a fixed time zone, UTC, so tests read the same everywhere. */
class FakeLocalTimeFormatter : LocalTimeFormatter {
    override fun compact(
        instant: Instant
    ): String = instant.toString().filter { it.isDigit() }.take(n = COMPACT_LENGTH)

    companion object {
        private const val COMPACT_LENGTH = 14
    }
}
