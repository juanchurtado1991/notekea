package app.notekea.sync.di

import app.notekea.sync.data.SettingsSyncSettingsStore
import app.notekea.sync.data.SyncEngines
import app.notekea.sync.domain.SyncSettingsStore
import app.notekea.sync.domain.VaultSyncs
import app.notekea.sync.presentation.SyncSettingsViewModel
import app.notekea.sync.presentation.SyncStatusViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Git sync (4.1, 4.2): each vault's engine for the app's life, its settings, the status bar and Settings → Sync. */
val syncModule = module {
    single<SyncSettingsStore> {
        SettingsSyncSettingsStore(
            settings = get(),
            dispatchers = get()
        )
    }

    single<VaultSyncs> {
        SyncEngines(
            repository = get(),
            tokens = get(),
            settingsStore = get(),
            edits = get(),
            dispatchers = get()
        )
    }

    viewModel { parameters ->
        SyncStatusViewModel(
            vaultPath = parameters.get(),
            engines = get(),
            noteContentStore = get()
        )
    }

    viewModel { parameters ->
        SyncSettingsViewModel(
            vaultPath = parameters.getOrNull(),
            engines = get(),
            settingsStore = get(),
            tokens = get(),
            sizes = get()
        )
    }
}
