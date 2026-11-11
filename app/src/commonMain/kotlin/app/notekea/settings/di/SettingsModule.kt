package app.notekea.settings.di

import app.notekea.settings.presentation.about.AboutViewModel
import app.notekea.settings.presentation.appearance.AppearanceViewModel
import app.notekea.settings.presentation.help.SettingsHelpViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Settings → Appearance, Ask about Settings beside it (its helper from the shell), and About. */
val settingsModule = module {
    viewModelOf(::AppearanceViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::SettingsHelpViewModel)
}
