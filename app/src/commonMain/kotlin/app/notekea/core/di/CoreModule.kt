package app.notekea.core.di

import app.notekea.core.appearance.data.SettingsAppearanceStore
import app.notekea.core.appearance.data.SettingsUiZoomStore
import app.notekea.core.appearance.data.SettingsWindowBoundsStore
import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.appearance.domain.GetUiZoomUseCase
import app.notekea.core.appearance.domain.SaveUiZoomUseCase
import app.notekea.core.appearance.domain.UiZoomStore
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.DescribeElapsedTimeUseCase
import app.notekea.core.preferences.data.SettingsAppPreferencesStore
import app.notekea.core.preferences.data.SettingsWebUpdateLog
import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.preferences.domain.WebUpdateLog
import app.notekea.core.presentation.platform.UiZoomViewModel
import app.notekea.core.presentation.platform.WindowBoundsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Cross-feature services: the system clock, read only here (`ARCHITECTURE.md` §5), time descriptions, file name rules
 * and the app's zoom, appearance and window bounds.
 */
val coreModule = module {
    single<Clock> { Clock.System }
    factoryOf(::DescribeElapsedTimeUseCase)
    factoryOf(::ValidateFileNameUseCase)

    single<UiZoomStore> {
        SettingsUiZoomStore(
            settings = get(),
            dispatchers = get()
        )
    }

    single<AppearanceStore> {
        SettingsAppearanceStore(
            settings = get(),
            dispatchers = get()
        )
    }

    single<AppPreferencesStore> {
        SettingsAppPreferencesStore(
            settings = get(),
            dispatchers = get()
        )
    }

    single<WebUpdateLog> {
        SettingsWebUpdateLog(
            settings = get(),
            dispatchers = get()
        )
    }

    single<WindowBoundsStore> {
        SettingsWindowBoundsStore(
            settings = get(),
            dispatchers = get()
        )
    }

    factoryOf(::GetUiZoomUseCase)
    factoryOf(::SaveUiZoomUseCase)
    viewModelOf(::WindowBoundsViewModel)
    viewModelOf(::UiZoomViewModel)
}
