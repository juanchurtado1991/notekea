package app.notekea.settings.di

import app.notekea.settings.presentation.about.AboutViewModel
import app.notekea.settings.presentation.appearance.AppearanceViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Settings → Appearance and About, on the stores and use cases of `core`. */
val settingsModule = module {
    viewModelOf(::AppearanceViewModel)
    viewModelOf(::AboutViewModel)
}
