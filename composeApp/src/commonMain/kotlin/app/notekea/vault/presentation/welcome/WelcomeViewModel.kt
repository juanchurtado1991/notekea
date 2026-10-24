package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.BaseViewModel
import app.notekea.vault.domain.CreateVaultUseCase
import app.notekea.vault.domain.GetRecentVaultsUseCase
import app.notekea.vault.domain.LocateVaultUseCase
import app.notekea.vault.domain.OpenVaultUseCase
import app.notekea.vault.domain.PickFolderUseCase
import app.notekea.vault.domain.RemoveRecentVaultUseCase
import app.notekea.vault.domain.RestoreLastVaultUseCase
import app.notekea.vault.domain.SuggestVaultLocationUseCase
import app.notekea.vault.domain.Vault
import app.notekea.vault.domain.VaultError
import co.touchlab.kermit.Logger

/**
 * Welcome flow (`ROADMAP.md` 1.3), the MVI reference screen (`ARCHITECTURE.md` §10): restores the last vault at
 * startup, opens, creates and locates vaults, and keeps the recent list fresh after every change. It has no one-shot
 * effects, so its effect type is [Nothing]. Picking a folder is a suspend call to the native dialog, not an effect.
 * Editing the new vault form clears its error, so a message never outlives the input it was about.
 */
class WelcomeViewModel(
    platformConventions: PlatformConventions,
    private val createVault: CreateVaultUseCase,
    private val getRecentVaults: GetRecentVaultsUseCase,
    private val locateVault: LocateVaultUseCase,
    private val openVault: OpenVaultUseCase,
    private val pickFolder: PickFolderUseCase,
    private val removeRecentVault: RemoveRecentVaultUseCase,
    private val restoreLastVault: RestoreLastVaultUseCase,
    private val suggestVaultLocation: SuggestVaultLocationUseCase
) : BaseViewModel<WelcomeState, WelcomeIntent, Nothing>(
        initialState = WelcomeState(platformConventions = platformConventions)
    ) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            val restored = restoreLastVault()
            val recentVaults = getRecentVaults()
            reduce { it.copy(isLoading = false, openedVault = restored, recentVaults = recentVaults) }
        }
    }

    override fun onIntent(
        intent: WelcomeIntent
    ) {
        when (intent) {
            is WelcomeIntent.OnCreateVaultClick -> startNewVault(suggestedName = intent.suggestedName)
            WelcomeIntent.OnErrorDismiss -> reduce { it.copy(error = null, hasUnexpectedError = false) }
            is WelcomeIntent.OnLocateVaultClick -> locate(missingPath = intent.missingPath)
            WelcomeIntent.OnNewVaultCancel -> reduce { it.copy(newVaultForm = null, error = null) }
            WelcomeIntent.OnNewVaultConfirm -> confirmNewVault()
            WelcomeIntent.OnNewVaultLocationClick -> changeNewVaultLocation()
            is WelcomeIntent.OnNewVaultNameChange -> updateNewVaultForm { it.copy(name = intent.name) }
            WelcomeIntent.OnOpenFolderClick -> pickAndOpen()
            is WelcomeIntent.OnRecentVaultClick -> openWith { openVault(path = intent.path) }
            is WelcomeIntent.OnRemoveRecentVaultClick -> remove(path = intent.path)
            WelcomeIntent.OnSwitchVaultClick -> switchVault()
        }
    }

    private fun changeNewVaultLocation() {
        val form = state.value.newVaultForm ?: return
        launchGuarded(onError = ::showUnexpectedError) {
            val parentPath = pickFolder(initialPath = form.parentPath) ?: return@launchGuarded
            updateNewVaultForm { it.copy(parentPath = parentPath) }
        }
    }

    private fun confirmNewVault() {
        val form = state.value.newVaultForm ?: return
        openWith { createVault(parentPath = form.parentPath, name = form.name) }
    }

    private fun locate(
        missingPath: String
    ) {
        launchGuarded(onError = ::showUnexpectedError) {
            val newPath = pickFolder(initialPath = null) ?: return@launchGuarded
            openNow { locateVault(missingPath = missingPath, newPath = newPath) }
        }
    }

    private suspend fun openNow(
        open: suspend () -> Outcome<Vault, VaultError>
    ) {
        reduce { it.copy(isBusy = true, error = null, hasUnexpectedError = false) }
        val outcome = open()
        val recentVaults = getRecentVaults()
        reduce { reduceOpenOutcome(state = it, outcome = outcome).copy(recentVaults = recentVaults) }
    }

    private fun openWith(
        open: suspend () -> Outcome<Vault, VaultError>
    ) {
        launchGuarded(onError = ::showUnexpectedError) { openNow(open = open) }
    }

    private fun pickAndOpen() {
        launchGuarded(onError = ::showUnexpectedError) {
            val path = pickFolder(initialPath = null) ?: return@launchGuarded
            openNow { openVault(path = path) }
        }
    }

    private fun remove(
        path: String
    ) {
        launchGuarded(onError = ::showUnexpectedError) {
            removeRecentVault(path = path)
            val recentVaults = getRecentVaults()
            reduce { it.copy(recentVaults = recentVaults) }
        }
    }

    private fun showUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(isLoading = false, isBusy = false, hasUnexpectedError = true) }
    }

    private fun startNewVault(
        suggestedName: String
    ) {
        launchGuarded(onError = ::showUnexpectedError) {
            val parentPath = suggestVaultLocation()
            val form = NewVaultForm(name = suggestedName, parentPath = parentPath)
            reduce { it.copy(newVaultForm = form, error = null) }
        }
    }

    private fun switchVault() {
        launchGuarded(onError = ::showUnexpectedError) {
            val recentVaults = getRecentVaults()
            reduce { it.copy(openedVault = null, recentVaults = recentVaults) }
        }
    }

    private fun updateNewVaultForm(
        transform: (NewVaultForm) -> NewVaultForm
    ) {
        reduce { state -> state.copy(newVaultForm = state.newVaultForm?.let(transform), error = null) }
    }

    companion object {
        private const val LOG_TAG = "WelcomeViewModel"
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the welcome flow"
    }
}
