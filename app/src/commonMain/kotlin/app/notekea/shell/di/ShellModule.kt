package app.notekea.shell.di

import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.CardMaker
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.chat.domain.sources.SourceSites
import app.notekea.settings.domain.SettingsHelpSource
import app.notekea.shell.data.SettingsWorkspaceStore
import app.notekea.shell.data.SyncVaultImporter
import app.notekea.shell.data.chat.CardsForChat
import app.notekea.shell.data.chat.ClipsForChat
import app.notekea.shell.data.chat.SettingsHelpFromGuide
import app.notekea.shell.data.chat.SourceSitesForChat
import app.notekea.shell.data.chat.VaultActionFinder
import app.notekea.shell.data.chat.VaultActionRunner
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.shell.domain.WorkspaceStore
import app.notekea.shell.presentation.onboarding.OnboardingViewModel
import app.notekea.shell.presentation.workspace.WorkspaceTools
import app.notekea.shell.presentation.workspace.WorkspaceViewModel
import app.notekea.sync.domain.VaultEdits
import app.notekea.tree.domain.FileWatcher
import app.notekea.vault.domain.VaultImporter
import kotlinx.coroutines.flow.map
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The window around the features: its panes, tabs and side panel, kept per vault.
 */
val shellModule = module {
    single<WorkspaceStore> {
        SettingsWorkspaceStore(
            settings = get(),
            dispatchers = get()
        )
    }

    factoryOf(::GetWorkspaceUseCase)
    factoryOf(::GetNoteCoverUseCase)
    factoryOf(::WorkspaceTools)
    factoryOf(::GetSidePanelUseCase)
    factoryOf(::SaveSidePanelUseCase)
    factoryOf(::VaultActionFinder)

    // Sync commits after a pause in the vault's changes, which the tree's watcher sees.
    single<VaultEdits> {
        val watcher = get<FileWatcher>()

        VaultEdits { vaultPath ->
            watcher.watch(vaultPath = vaultPath).map { }
        }
    }

    single<CardMaker> { CardsForChat(makeCards = get()) }

    single<PageReader> { get<ClipsForChat>() }
    single<WebPageClipper> { get<ClipsForChat>() }

    single<SourceSites> {
        SourceSitesForChat(
            search = get(),
            noteContentStore = get(),
            localCalendar = get(),
            clock = get()
        )
    }

    single<SettingsHelpSource> {
        SettingsHelpFromGuide(
            answerHelp = get(),
            conventions = get()
        )
    }

    single {
        ClipsForChat(
            fetcher = get(),
            extractor = get(),
            clipWebPage = get(),
            findSection = get(),
            clipSection = get(),
            describeSection = get(),
            localCalendar = get(),
            clock = get()
        )
    }

    single<VaultImporter> { SyncVaultImporter(vaultSyncs = get()) }

    single<ActionRunner> {
        VaultActionRunner(
            get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(), get(),
            get()
        )
    }

    viewModel { parameters ->
        WorkspaceViewModel(
            vaultPath = parameters.get(),
            tools = get()
        )
    }

    viewModelOf(::OnboardingViewModel)
}
