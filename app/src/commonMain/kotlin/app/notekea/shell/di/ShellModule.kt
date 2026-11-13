package app.notekea.shell.di

import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.CardMaker
import app.notekea.chat.domain.plan.PageReader
import app.notekea.chat.domain.plan.WebPageClipper
import app.notekea.settings.domain.SettingsHelpSource
import app.notekea.shell.data.CardsForChat
import app.notekea.shell.data.ClipsForChat
import app.notekea.shell.data.SettingsHelpFromGuide
import app.notekea.shell.data.SettingsWorkspaceStore
import app.notekea.shell.data.VaultActionFinder
import app.notekea.shell.data.VaultActionRunner
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.shell.domain.WorkspaceStore
import app.notekea.shell.presentation.WorkspaceTools
import app.notekea.shell.presentation.WorkspaceViewModel
import app.notekea.shell.presentation.onboarding.OnboardingViewModel
import app.notekea.sync.domain.VaultEdits
import app.notekea.tree.domain.FileWatcher
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

    single<ActionRunner> {
        VaultActionRunner(
            get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get(), get(), get()
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
