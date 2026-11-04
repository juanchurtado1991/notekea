package app.notekea.search.di

import app.notekea.search.data.SqlDelightIndexStateStore
import app.notekea.search.domain.IndexStateStore
import app.notekea.search.domain.IndexVaultUseCase
import app.notekea.search.domain.PlanIndexUseCase
import app.notekea.search.domain.SearchNoteContentUseCase
import app.notekea.search.domain.SearchNotesUseCase
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.search.presentation.switcher.QuickSwitcherViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The search index and the quick switcher.
 */
val searchModule = module {
    single<IndexStateStore> { SqlDelightIndexStateStore(database = get(), dispatchers = get()) }
    factoryOf(::PlanIndexUseCase)
    factoryOf(::IndexVaultUseCase)
    viewModel { parameters ->
        SearchIndexViewModel(
            vaultPath = parameters.get(),
            indexVault = get(),
            vectorIndex = get(),
            indexStateStore = get(),
            describeElapsedTime = get(),
            clock = get()
        )
    }
    factoryOf(::SearchNoteContentUseCase)
    factoryOf(::SearchNotesUseCase)
    viewModel { parameters ->
        QuickSwitcherViewModel(
            vaultPath = parameters.get(),
            searchNotes = get(),
            searchNoteContent = get(),
            dispatchers = get()
        )
    }
}
