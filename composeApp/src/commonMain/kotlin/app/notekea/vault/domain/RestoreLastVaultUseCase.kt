package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * Reopens the most recent vault at startup. Returns null when there is none or it cannot be opened: the welcome
 * screen then shows it in the recent list with its problem, instead of greeting the user with an error.
 */
class RestoreLastVaultUseCase(
    private val recentVaultsStore: RecentVaultsStore,
    private val openVault: OpenVaultUseCase
) {
    suspend operator fun invoke(): OpenedVault? {
        val lastVault = recentVaultsStore.read().firstOrNull() ?: return null
        return (openVault(path = lastVault.path) as? Outcome.Success)?.value
    }
}
