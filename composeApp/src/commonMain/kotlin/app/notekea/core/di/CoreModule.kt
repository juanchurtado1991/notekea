package app.notekea.core.di

import app.notekea.core.domain.files.ValidateFileNameUseCase
import app.notekea.core.domain.time.DescribeElapsedTimeUseCase
import app.notekea.core.appearance.data.SettingsAppearanceStore
import app.notekea.core.appearance.data.SettingsUiScaleStore
import app.notekea.core.appearance.data.SettingsWindowBoundsStore
import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.appearance.domain.GetUiScaleUseCase
import app.notekea.core.appearance.domain.SaveUiScaleUseCase
import app.notekea.core.appearance.domain.UiScaleStore
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.presentation.platform.WindowBoundsViewModel
import app.notekea.core.presentation.settings.AppearanceViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Cross-feature services: the system clock, read only here (`ARCHITECTURE.md` §5), time descriptions, file name rules
 * and the app's zoom, appearance and window bounds, with Settings → Appearance.
 */
val coreModule = module {
    single<Clock> { Clock.System }
    factoryOf(::DescribeElapsedTimeUseCase)
    factoryOf(::ValidateFileNameUseCase)
    single<UiScaleStore> { SettingsUiScaleStore(settings = get(), dispatchers = get()) }
    single<AppearanceStore> { SettingsAppearanceStore(settings = get(), dispatchers = get()) }
    single<WindowBoundsStore> { SettingsWindowBoundsStore(settings = get(), dispatchers = get()) }
    factoryOf(::GetUiScaleUseCase)
    factoryOf(::SaveUiScaleUseCase)
    viewModelOf(::AppearanceViewModel)
    viewModelOf(::WindowBoundsViewModel)
}
