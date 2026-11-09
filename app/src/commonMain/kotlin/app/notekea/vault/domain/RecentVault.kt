package app.notekea.vault.domain

import kotlin.time.Instant

/** A vault the user opened before, as remembered between runs. */
data class RecentVault(
    val path: String,
    val lastOpenedAt: Instant
)
