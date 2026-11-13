package app.notekea.core.preferences.domain

/** The day each vault's web sources were last updated on their own, so the next one waits its turn. */
interface WebUpdateLog {
    /** The day's number (days since 1970) of the vault at [vaultPath]'s last update; null when there was none. */
    suspend fun lastDay(vaultPath: String): Long?

    suspend fun mark(
        vaultPath: String,
        day: Long
    )
}
