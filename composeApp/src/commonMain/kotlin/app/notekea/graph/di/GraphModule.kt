package app.notekea.graph.di

import app.notekea.graph.data.SettingsGraphPreferencesStore
import app.notekea.graph.domain.BuildNoteGraphUseCase
import app.notekea.graph.domain.GraphPreferencesStore
import app.notekea.graph.presentation.GraphViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The graph tab: its data from the vault's links and one view model per vault. */
val graphModule = module {
    single<GraphPreferencesStore> { SettingsGraphPreferencesStore(settings = get(), dispatchers = get()) }
    factoryOf(::BuildNoteGraphUseCase)
    viewModel { parameters ->
        GraphViewModel(
            vaultPath = parameters.get(),
            buildNoteGraph = get(),
            findRelatedNotes = get(),
            noteTextCache = get(),
            markdownLineStripper = get(),
            preferencesStore = get(),
            dispatchers = get()
        )
    }
}
