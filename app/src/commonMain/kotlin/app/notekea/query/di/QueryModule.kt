package app.notekea.query.di

import app.notekea.query.data.GhostQueryJsonReader
import app.notekea.query.data.SettingsQueryPreferencesStore
import app.notekea.query.domain.BuildQueryCatalogUseCase
import app.notekea.query.domain.QueryJsonReader
import app.notekea.query.domain.QueryPreferencesStore
import app.notekea.query.domain.QueryYaml
import app.notekea.query.domain.RunQueryUseCase
import app.notekea.query.domain.SaveQueryUseCase
import app.notekea.query.domain.SetPropertyUseCase
import app.notekea.query.domain.TranslateQueryUseCase
import app.notekea.query.presentation.QueryViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** The vault's queries: their results from the note cache, their writes to notes and one view model per vault. */
val queryModule = module {
    single<QueryPreferencesStore> {
        SettingsQueryPreferencesStore(
            settings = get(),
            queryYaml = get(),
            dispatchers = get()
        )
    }

    factoryOf(::BuildQueryCatalogUseCase)
    factoryOf(::QueryYaml)
    factoryOf(::RunQueryUseCase)
    factoryOf(::SaveQueryUseCase)
    factoryOf(::SetPropertyUseCase)
    factoryOf(::TranslateQueryUseCase)
    factory<QueryJsonReader> { GhostQueryJsonReader() }

    viewModel { parameters ->
        QueryViewModel(
            vaultPath = parameters.get(),
            runQuery = get(),
            buildCatalog = get(),
            setProperty = get(),
            saveQuery = get(),
            preferencesStore = get(),
            translateQuery = get(),
            localCalendar = get(),
            clock = get(),
            dispatchers = get()
        )
    }
}
