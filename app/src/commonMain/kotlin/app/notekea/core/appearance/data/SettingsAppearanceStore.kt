package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode
import app.notekea.core.domain.AppDispatchers
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * [AppearanceStore] on Multiplatform Settings: `app.theme` and `app.theme_mode`, by their ids, and `app.reduce_motion`. It is read once, on the
 * first [observe], and then held, so every window sees each [save] at once.
 */
class SettingsAppearanceStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : AppearanceStore {
    private val current = MutableStateFlow<Appearance?>(value = null)

    override fun observe(): Flow<Appearance> = flow {
        if (current.value == null) current.compareAndSet(expect = null, update = read())
        emitAll(current.filterNotNull())
    }

    override suspend fun save(
        appearance: Appearance
    ) {
        withContext(context = dispatchers.io) {
            settings.putString(key = THEME_KEY, value = appearance.theme.id)
            settings.putString(key = MODE_KEY, value = appearance.mode.id)
            settings.putBoolean(key = REDUCE_MOTION_KEY, value = appearance.reduceMotion)
        }
        current.value = appearance
    }

    private suspend fun read(): Appearance = withContext(context = dispatchers.io) {
        Appearance(
            theme = ColorTheme.fromId(id = settings.getStringOrNull(key = THEME_KEY)),
            mode = ThemeMode.fromId(id = settings.getStringOrNull(key = MODE_KEY)),
            reduceMotion = settings.getBoolean(key = REDUCE_MOTION_KEY, defaultValue = false)
        )
    }

    private companion object {
        const val MODE_KEY = "app.theme_mode"
        const val REDUCE_MOTION_KEY = "app.reduce_motion"
        const val THEME_KEY = "app.theme"
    }
}
