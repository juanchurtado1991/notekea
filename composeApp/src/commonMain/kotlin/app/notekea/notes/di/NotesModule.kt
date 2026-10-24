package app.notekea.notes.di

import app.notekea.notes.data.GhostFrontmatterParser
import app.notekea.notes.data.SettingsTreePreferencesStore
import app.notekea.notes.domain.CreateFolderUseCase
import app.notekea.notes.domain.CreateNoteUseCase
import app.notekea.notes.domain.DeleteEntryUseCase
import app.notekea.notes.domain.FlattenTreeUseCase
import app.notekea.notes.domain.GetTreePreferencesUseCase
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.RenameEntryUseCase
import app.notekea.notes.domain.RevealEntryUseCase
import app.notekea.notes.domain.SaveTreePreferencesUseCase
import app.notekea.notes.domain.TreePreferencesStore
import app.notekea.notes.presentation.tree.VaultTreeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Notes feature: the vault tree use cases, its preferences on the app settings and the sidebar ViewModel, created
 * once per vault with the vault path as its parameter. Platform adapters are bound by the platform module.
 */
val notesModule = module {
    single<TreePreferencesStore> { SettingsTreePreferencesStore(settings = get(), dispatchers = get()) }
    factoryOf(::CreateFolderUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::DeleteEntryUseCase)
    factoryOf(::FlattenTreeUseCase)
    factoryOf(::GetTreePreferencesUseCase)
    factoryOf(::GhostFrontmatterParser)
    factoryOf(::NaturalNameComparator)
    factoryOf(::ObserveVaultTreeUseCase)
    factoryOf(::RenameEntryUseCase)
    factoryOf(::RevealEntryUseCase)
    factoryOf(::SaveTreePreferencesUseCase)
    viewModel { parameters ->
        VaultTreeViewModel(
            vaultPath = parameters.get(),
            platformConventions = get(),
            observeVaultTree = get(),
            flattenTree = get(),
            createNote = get(),
            createFolder = get(),
            renameEntry = get(),
            deleteEntry = get(),
            revealEntry = get(),
            getTreePreferences = get(),
            saveTreePreferences = get()
        )
    }
}
