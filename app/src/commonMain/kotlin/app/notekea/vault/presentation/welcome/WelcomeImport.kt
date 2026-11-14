package app.notekea.vault.presentation.welcome

import app.notekea.core.domain.Outcome
import app.notekea.vault.domain.FolderPicker
import app.notekea.vault.domain.ImportVaultUseCase
import app.notekea.vault.domain.Vault
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.VaultFileSystem
import kotlinx.coroutines.Job

/**
 * The welcome screen's "Import from a repository" (the developer's request of 2026-11-14), for [WelcomeViewModel]:
 * its panel, the folder's name following the address until one is typed, and the import, which Cancel stops half way.
 * [open] opens a vault as the screen's other actions do, with the screen busy meanwhile.
 */
internal class WelcomeImport(
    private val importVault: ImportVaultUseCase,
    private val vaultFileSystem: VaultFileSystem,
    private val folderPicker: FolderPicker,
    private val state: () -> WelcomeState,
    private val reduce: ((WelcomeState) -> WelcomeState) -> Unit,
    private val launch: (suspend () -> Unit) -> Job,
    private val open: suspend (suspend () -> Outcome<Vault, VaultError>) -> Unit
) {
    private var job: Job? = null

    fun on(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.OnImportClick -> start()

            WelcomeIntent.OnImportCancel -> cancel()

            WelcomeIntent.OnImportConfirm -> confirm()

            WelcomeIntent.OnImportLocationClick -> changeLocation()

            is WelcomeIntent.OnImportNameChange -> update {
                it.copy(
                    name = intent.name,
                    isNamed = true
                )
            }

            is WelcomeIntent.OnImportTokenChange -> update { it.copy(token = intent.token) }

            is WelcomeIntent.OnImportUrlChange -> update { form ->
                // The folder is named after the repository until the person names it.
                val followed = ImportVaultUseCase.nameOf(remoteUrl = intent.url)
                val name = if (form.isNamed) form.name else followed

                form.copy(
                    url = intent.url,
                    name = name
                )
            }

            else -> Unit
        }
    }

    private fun start() {
        launch {
            val parentPath = vaultFileSystem.suggestedParentFolder()

            reduce {
                it.copy(
                    importForm = ImportVaultForm(
                        url = "",
                        token = "",
                        name = "",
                        parentPath = parentPath
                    ),
                    newVaultForm = null,
                    error = null
                )
            }
        }
    }

    // An import under way stops, and the panel stays to try again; with none, the panel closes. The screen stays busy
    // until the import has really ended: Git's work is not interrupted at once, and Import pressed meanwhile would
    // do nothing (code review, 2026-11-14).
    private fun cancel() {
        val running = job?.takeIf { it.isActive }

        if (running != null) {
            running.cancel()

            launch {
                running.join()
                reduce { it.copy(isBusy = false) }
            }
        } else {
            reduce {
                it.copy(
                    importForm = null,
                    error = null
                )
            }
        }
    }

    private fun confirm() {
        val form = state().importForm ?: return
        if (job?.isActive == true) return

        job = launch {
            open {
                importVault(
                    parentPath = form.parentPath,
                    name = form.name,
                    remoteUrl = form.url,
                    token = form.token
                )
            }
        }
    }

    private fun changeLocation() {
        val form = state().importForm ?: return

        launch {
            val parentPath = folderPicker.pickFolder(initialPath = form.parentPath) ?: return@launch
            update { it.copy(parentPath = parentPath) }
        }
    }

    // Editing the form clears its error, as the new vault's form does.
    private fun update(transform: (ImportVaultForm) -> ImportVaultForm) {
        reduce { state ->
            state.copy(
                importForm = state.importForm?.let(transform),
                error = null
            )
        }
    }
}
