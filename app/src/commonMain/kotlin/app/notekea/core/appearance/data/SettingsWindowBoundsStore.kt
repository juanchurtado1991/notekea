package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.WindowBounds
import app.notekea.core.appearance.domain.WindowBoundsStore
import app.notekea.core.domain.AppDispatchers
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [WindowBoundsStore] on Multiplatform Settings, the same for every vault: `app.window_*`. */
class SettingsWindowBoundsStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : WindowBoundsStore {
    override suspend fun bounds(): WindowBounds? = withContext(context = dispatchers.io) {
        val width = settings.getIntOrNull(key = WIDTH_KEY)
        val height = settings.getIntOrNull(key = HEIGHT_KEY)
        if (width == null || height == null) return@withContext null
        WindowBounds(
            widthDp = width,
            heightDp = height,
            xDp = settings.getIntOrNull(key = X_KEY),
            yDp = settings.getIntOrNull(key = Y_KEY),
            isMaximized = settings.getBoolean(key = MAXIMIZED_KEY, defaultValue = false)
        )
    }

    override suspend fun save(
        bounds: WindowBounds
    ) = withContext(context = dispatchers.io) {
        settings.putInt(key = WIDTH_KEY, value = bounds.widthDp)
        settings.putInt(key = HEIGHT_KEY, value = bounds.heightDp)
        putOrRemove(key = X_KEY, value = bounds.xDp)
        putOrRemove(key = Y_KEY, value = bounds.yDp)
        settings.putBoolean(key = MAXIMIZED_KEY, value = bounds.isMaximized)
    }

    private fun putOrRemove(
        key: String,
        value: Int?
    ) {
        if (value == null) settings.remove(key = key) else settings.putInt(key = key, value = value)
    }

    private companion object {
        const val HEIGHT_KEY = "app.window_height"
        const val MAXIMIZED_KEY = "app.window_maximized"
        const val WIDTH_KEY = "app.window_width"
        const val X_KEY = "app.window_x"
        const val Y_KEY = "app.window_y"
    }
}
