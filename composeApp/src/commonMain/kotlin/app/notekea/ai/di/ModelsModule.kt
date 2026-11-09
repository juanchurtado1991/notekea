package app.notekea.ai.di

import app.notekea.ai.data.BundledModelCatalogSource
import app.notekea.ai.data.SettingsActiveModelStore
import app.notekea.ai.domain.ActiveModelStore
import app.notekea.ai.domain.InstallModelUseCase
import app.notekea.ai.domain.ModelCatalogSource
import app.notekea.ai.domain.RecommendModelUseCase
import app.notekea.ai.presentation.ModelManagerViewModel
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
