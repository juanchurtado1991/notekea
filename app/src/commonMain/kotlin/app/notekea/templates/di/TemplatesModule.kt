package app.notekea.templates.di

import app.notekea.templates.domain.CreateTemplatesFolderUseCase
import app.notekea.templates.domain.GetTemplateSettingsUseCase
import app.notekea.templates.presentation.TemplatePickerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The template picker and its settings.
 */
val templatesModule = module {
    factoryOf(::GetTemplateSettingsUseCase)
    factoryOf(::CreateTemplatesFolderUseCase)
    viewModel { parameters ->
        TemplatePickerViewModel(
            vaultPath = parameters.get(),
            getTemplateSettings = get(),
            createTemplatesFolder = get(),
            fuzzyMatcher = get()
        )
    }
}
