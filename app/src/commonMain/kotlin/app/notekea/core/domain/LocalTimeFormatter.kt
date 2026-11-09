package app.notekea.core.domain

import kotlin.time.Instant

/** Writes a moment in the user's own time zone, which only the platform knows. */
interface LocalTimeFormatter {
    /** [instant] as `yyyyMMddHHmmss` in the local time zone, as Obsidian names pasted images. */
    fun compact(
        instant: Instant
    ): String
}
