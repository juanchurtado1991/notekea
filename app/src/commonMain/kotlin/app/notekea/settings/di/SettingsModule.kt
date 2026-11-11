package app.notekea.settings.di

import app.notekea.settings.presentation.about.AboutViewModel
import app.notekea.settings.presentation.appearance.AppearanceViewModel
import app.notekea.settings.presentation.files.FilesSettingsViewModel
import app.notekea.settings.presentation.help.SettingsHelpViewModel
import app.notekea.settings.presentation.preferences.PreferencesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Settings' own sections (General, Appearance, Editor, Files, About) and Ask about Settings beside them. */
val settingsModule = module {
    viewModelOf(::AppearanceViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::SettingsHelpViewModel)
    viewModelOf(::PreferencesViewModel)

    viewModel { parameters ->
        FilesSettingsViewModel(
            reader = get(),
            writer = get(),
            vaultPath = parameters.getOrNull()
        )
    }
}
