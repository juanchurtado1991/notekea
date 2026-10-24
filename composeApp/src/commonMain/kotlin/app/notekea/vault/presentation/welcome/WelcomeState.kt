package app.notekea.vault.presentation.welcome

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.vault.domain.OpenedVault
import app.notekea.vault.domain.RecentVaultStatus
import app.notekea.vault.domain.VaultError

/**
 * State of the welcome flow. [isLoading] covers the startup restore of the last vault, so the welcome screen never
 * flashes before it; [isBusy] disables the actions while a vault opens; [openedVault] switches to the vault view.
 */
data class WelcomeState(
    val platformConventions: PlatformConventions,
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val recentVaults: List<RecentVaultStatus> = emptyList(),
    val newVaultForm: NewVaultForm? = null,
    val error: VaultError? = null,
    val hasUnexpectedError: Boolean = false,
    val openedVault: OpenedVault? = null
)
