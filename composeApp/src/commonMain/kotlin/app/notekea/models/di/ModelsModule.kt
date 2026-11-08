package app.notekea.models.di

import app.notekea.models.data.BundledModelCatalogSource
import app.notekea.models.data.SettingsActiveModelStore
import app.notekea.models.domain.ActiveModelStore
import app.notekea.models.domain.InstallModelUseCase
import app.notekea.models.domain.ModelCatalogSource
import app.notekea.models.domain.RecommendModelUseCase
import app.notekea.models.presentation.ModelManagerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The model manager (`ROADMAP.md` 3.1): the bundled catalog, the model in use and Settings → AI model. */
val modelsModule = module {
    single<ModelCatalogSource> { BundledModelCatalogSource(dispatchers = get()) }
    single<ActiveModelStore> { SettingsActiveModelStore(settings = get(), dispatchers = get()) }
    factoryOf(::InstallModelUseCase)
    factoryOf(::RecommendModelUseCase)
    viewModelOf(::ModelManagerViewModel)
}
