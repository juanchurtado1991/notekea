package app.notekea.vault.di

import app.notekea.vault.data.SettingsRecentVaultsStore
import app.notekea.vault.domain.CreateVaultUseCase
import app.notekea.vault.domain.GetRecentVaultsUseCase
import app.notekea.vault.domain.LocateVaultUseCase
import app.notekea.vault.domain.OpenVaultUseCase
import app.notekea.vault.domain.PickFolderUseCase
import app.notekea.vault.domain.RecentVaultsStore
import app.notekea.vault.domain.RemoveRecentVaultUseCase
import app.notekea.vault.domain.RestoreLastVaultUseCase
import app.notekea.vault.domain.SuggestVaultLocationUseCase
import app.notekea.vault.domain.ValidateFileNameUseCase
import app.notekea.vault.presentation.welcome.WelcomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Vault feature: recent vaults on the app settings, the vault use cases and the welcome flow. */
val vaultModule = module {
    single<RecentVaultsStore> { SettingsRecentVaultsStore(settings = get(), dispatchers = get()) }
    factoryOf(::CreateVaultUseCase)
    factoryOf(::GetRecentVaultsUseCase)
    factoryOf(::LocateVaultUseCase)
    factoryOf(::OpenVaultUseCase)
    factoryOf(::PickFolderUseCase)
    factoryOf(::RemoveRecentVaultUseCase)
    factoryOf(::RestoreLastVaultUseCase)
    factoryOf(::SuggestVaultLocationUseCase)
    factoryOf(::ValidateFileNameUseCase)
    viewModelOf(::WelcomeViewModel)
}
