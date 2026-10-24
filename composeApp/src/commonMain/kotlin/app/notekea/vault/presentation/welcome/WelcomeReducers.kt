package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.Outcome
import app.notekea.vault.domain.OpenedVault
import app.notekea.vault.domain.VaultError

// Pure state transitions of the welcome flow, unit tested directly (`ARCHITECTURE.md` §4).

internal fun reduceOpenOutcome(
    state: WelcomeState,
    outcome: Outcome<OpenedVault, VaultError>
): WelcomeState = when (outcome) {
    is Outcome.Failure -> state.copy(isBusy = false, error = outcome.error)
    is Outcome.Success -> state.copy(isBusy = false, openedVault = outcome.value, newVaultForm = null, error = null)
}
