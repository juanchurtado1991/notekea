package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.Outcome
import app.notekea.vault.domain.VaultError
import kotlin.test.Test
import kotlin.test.assertEquals

class WelcomeReducersTest {
    private val busy = WelcomePreviewData.state().copy(isBusy = true, newVaultForm = WelcomePreviewData.newVaultForm)

    @Test
    fun `a successful open shows the vault and closes the form`() {
        assertEquals(
            expected = busy.copy(isBusy = false, openedVault = WelcomePreviewData.openedVault, newVaultForm = null),
            actual = reduceOpenOutcome(state = busy, outcome = Outcome.Success(value = WelcomePreviewData.openedVault))
        )
    }

    @Test
    fun `a failed open keeps the form and shows the error`() {
        assertEquals(
            expected = busy.copy(isBusy = false, error = VaultError.InvalidName),
            actual = reduceOpenOutcome(state = busy, outcome = Outcome.Failure(error = VaultError.InvalidName))
        )
    }
}
