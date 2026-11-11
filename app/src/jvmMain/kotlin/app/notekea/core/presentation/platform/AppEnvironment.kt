package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.platform.domain.SystemAppearance
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.core.platform.domain.SystemMotion
import app.notekea.core.preferences.domain.AppPreferencesStore

/** What every window of the app reads from the system and the settings ([app.notekea.NotekeaAppLocals]). */
class AppEnvironment(
    val systemAppearance: SystemAppearance,
    val systemMotion: SystemMotion,
    val systemEnvironment: SystemEnvironment,
    val appearanceStore: AppearanceStore,
    val preferencesStore: AppPreferencesStore
)
