package app.notekea.shell.di

import app.notekea.shell.data.SettingsWorkspaceStore
import app.notekea.shell.domain.GetNoteCoverUseCase
import app.notekea.shell.domain.GetSidePanelUseCase
import app.notekea.shell.domain.GetWorkspaceUseCase
import app.notekea.shell.domain.SaveSidePanelUseCase
import app.notekea.shell.domain.WorkspaceStore
import app.notekea.shell.presentation.WorkspaceTools
import app.notekea.shell.presentation.WorkspaceViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
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

    viewModel { parameters ->
        WorkspaceViewModel(
            vaultPath = parameters.get(),
            tools = get()
        )
    }
}
