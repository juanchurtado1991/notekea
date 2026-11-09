package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/** Opens the folder the user found for a missing recent vault and, if it opens, forgets the old path. */
class LocateVaultUseCase(
    private val openVault: OpenVaultUseCase,
    private val removeRecentVault: RemoveRecentVaultUseCase
) {
    suspend operator fun invoke(
        missingPath: String,
        newPath: String
    ): Outcome<Vault, VaultError> {
        val opened = openVault(path = newPath)
        if (opened is Outcome.Success && newPath != missingPath) removeRecentVault(path = missingPath)
        return opened
    }
}
