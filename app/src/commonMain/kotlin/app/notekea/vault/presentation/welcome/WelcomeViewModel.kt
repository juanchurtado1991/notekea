package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.Outcome
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.presentation.BaseViewModel
import app.notekea.vault.domain.CreateVaultUseCase
import app.notekea.vault.domain.FolderPicker
import app.notekea.vault.domain.GetRecentVaultsUseCase
import app.notekea.vault.domain.LocateVaultUseCase
import app.notekea.vault.domain.OpenVaultUseCase
import app.notekea.vault.domain.RemoveRecentVaultUseCase
import app.notekea.vault.domain.RestoreLastVaultUseCase
import app.notekea.vault.domain.SampleVaultSource
import app.notekea.vault.domain.Vault
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.VaultFileSystem
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first

/**
 * Welcome flow, the MVI reference screen (`ARCHITECTURE.md` §10): restores the last vault at startup unless General
 * says not to, opens, creates and locates vaults, and keeps the recent list fresh; it has no one-shot effects. Picking
 * a folder is a suspend call to the native dialog; editing the new vault form clears its error.
 */
class WelcomeViewModel(
    platformConventions: PlatformConventions,
    private val createVault: CreateVaultUseCase,
    private val getRecentVaults: GetRecentVaultsUseCase,
    private val locateVault: LocateVaultUseCase,
    private val openVault: OpenVaultUseCase,
    private val folderPicker: FolderPicker,
    private val removeRecentVault: RemoveRecentVaultUseCase,
    private val restoreLastVault: RestoreLastVaultUseCase,
    private val vaultFileSystem: VaultFileSystem,
    private val preferencesStore: AppPreferencesStore,
    private val sampleVault: SampleVaultSource
) : BaseViewModel<WelcomeState, WelcomeIntent, Nothing>(
    initialState = WelcomeState(platformConventions = platformConventions)
) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::showUnexpectedError) {
            // Settings → General can ask for the welcome screen instead of the last vault at startup.
            val opensLast = preferencesStore
                .observe()
                .first()
                .opensLastVault

            val restored = if (opensLast) restoreLastVault() else null
            val recentVaults = getRecentVaults()

            reduce {
                it.copy(
                    isLoading = false,
                    openedVault = restored,
                    recentVaults = recentVaults
                )
            }
        }
    }

    override fun handleIntent(intent: WelcomeIntent) {
        when (intent) {
            is WelcomeIntent.OnCreateVaultClick -> startNewVault(
                suggestedName = intent.suggestedName
            )

            WelcomeIntent.OnErrorDismiss -> reduce {
                it.copy(
                    error = null,
                    hasUnexpectedError = false
                )
            }

            is WelcomeIntent.OnLocateVaultClick -> locate(missingPath = intent.missingPath)

            WelcomeIntent.OnNewVaultCancel -> reduce {
                it.copy(
                    newVaultForm = null,
                    error = null
                )
            }

            WelcomeIntent.OnNewVaultConfirm -> confirmNewVault()

            WelcomeIntent.OnNewVaultLocationClick -> changeNewVaultLocation()

            is WelcomeIntent.OnNewVaultNameChange -> updateNewVaultForm {
                it.copy(name = intent.name)
            }

            WelcomeIntent.OnOpenFolderClick -> pickAndOpen()

            is WelcomeIntent.OnRecentVaultClick -> openWith { openVault(path = intent.path) }

            WelcomeIntent.OnSampleVaultClick -> openWith { openSample() }

            is WelcomeIntent.OnRemoveRecentVaultClick -> remove(path = intent.path)

            WelcomeIntent.OnSwitchVaultClick -> switchVault()
        }
    }

    private fun changeNewVaultLocation() {
        val form = state.value.newVaultForm ?: return

        launchGuarded(onError = ::showUnexpectedError) {
            val parentPath =
                folderPicker.pickFolder(initialPath = form.parentPath) ?: return@launchGuarded

            updateNewVaultForm { it.copy(parentPath = parentPath) }
        }
    }

    private fun confirmNewVault() {
        val form = state.value.newVaultForm ?: return

        openWith {
            createVault(
                parentPath = form.parentPath,
                name = form.name
            )
        }
    }

    private fun locate(missingPath: String) {
        launchGuarded(onError = ::showUnexpectedError) {
            val newPath = folderPicker.pickFolder(initialPath = null) ?: return@launchGuarded

            openNow {
                locateVault(
                    missingPath = missingPath,
                    newPath = newPath
                )
            }
        }
    }

    private suspend fun openNow(open: suspend () -> Outcome<Vault, VaultError>) {
        reduce {
            it.copy(
                isBusy = true,
                error = null,
                hasUnexpectedError = false
            )
        }

        val outcome = open()
        val recentVaults = getRecentVaults()

        reduce {
            reduceOpenOutcome(
                state = it,
                outcome = outcome
            ).copy(recentVaults = recentVaults)
        }
    }

    private fun openWith(open: suspend () -> Outcome<Vault, VaultError>) {
        launchGuarded(onError = ::showUnexpectedError) { openNow(open = open) }
    }

    // A new folder for the sample vault in Documents, numbered when one is there already, filled and opened.
    private suspend fun openSample(): Outcome<Vault, VaultError> {
        val parent = vaultFileSystem.suggestedParentFolder()

        val folder = sampleFolderIn(parent = parent)
            ?: return Outcome.Failure(error = VaultError.CreationFailed(path = parent))

        return when (val copied = sampleVault.copyTo(folder = folder)) {
            is Outcome.Success -> openVault(path = folder)
            is Outcome.Failure -> copied
        }
    }

    // "Notekea Sample Vault", or "… 2" and on when one is there already with its files.
    private suspend fun sampleFolderIn(parent: String): String? {
        for (number in 1..MAX_SAMPLES) {
            val name = if (number == 1) SAMPLE_NAME else "$SAMPLE_NAME $number"

            val created = vaultFileSystem.createFolder(
                parentPath = parent,
                name = name
            )

            if (created is Outcome.Success) return created.value
        }

        return null
    }

    private fun pickAndOpen() {
        launchGuarded(onError = ::showUnexpectedError) {
            val path = folderPicker.pickFolder(initialPath = null) ?: return@launchGuarded

            openNow { openVault(path = path) }
        }
    }

    private fun remove(path: String) {
        launchGuarded(onError = ::showUnexpectedError) {
            removeRecentVault(path = path)
            val recentVaults = getRecentVaults()

            reduce { it.copy(recentVaults = recentVaults) }
        }
    }

    private fun showUnexpectedError(throwable: Throwable) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }

        reduce {
            it.copy(
                isLoading = false,
                isBusy = false,
                hasUnexpectedError = true
            )
        }
    }

    private fun startNewVault(suggestedName: String) {
        launchGuarded(onError = ::showUnexpectedError) {
            val parentPath = vaultFileSystem.suggestedParentFolder()

            val form = NewVaultForm(
                name = suggestedName,
                parentPath = parentPath
            )

            reduce {
                it.copy(
                    newVaultForm = form,
                    error = null
                )
            }
        }
    }

    private fun switchVault() {
        launchGuarded(onError = ::showUnexpectedError) {
            val recentVaults = getRecentVaults()

            reduce {
                it.copy(
                    openedVault = null,
                    recentVaults = recentVaults
                )
            }
        }
    }

    private fun updateNewVaultForm(transform: (NewVaultForm) -> NewVaultForm) {
        reduce { state ->
            state.copy(
                newVaultForm = state.newVaultForm?.let(transform),
                error = null
            )
        }
    }

    companion object {
        private const val LOG_TAG = "WelcomeViewModel"
        private const val SAMPLE_NAME = "Notekea Sample Vault"
        private const val MAX_SAMPLES = 20
        private const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error in the welcome flow"
    }
}
