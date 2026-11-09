package app.notekea.vault.domain

import app.notekea.core.domain.time.ElapsedTime

/** A recent vault ready to show: how long ago it was opened and whether its folder still exists. */
data class RecentVaultStatus(
    val vault: Vault,
    val lastOpened: ElapsedTime,
    val isMissing: Boolean
)
