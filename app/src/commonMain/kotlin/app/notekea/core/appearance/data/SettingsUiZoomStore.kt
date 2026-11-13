package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.UiZoomLevels
import app.notekea.core.appearance.domain.UiZoomStore
import app.notekea.core.domain.AppDispatchers
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [UiZoomStore] on Multiplatform Settings, the same for every vault: `app.ui_scale`. */
class SettingsUiZoomStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : UiZoomStore {
    override suspend fun scale(): Float = withContext(context = dispatchers.io) {
        settings.getFloat(
            key = SCALE_KEY,
            defaultValue = UiZoomLevels.DEFAULT
        )
    }

    override suspend fun setScale(scale: Float) = withContext(context = dispatchers.io) {
        settings.putFloat(
            key = SCALE_KEY,
            value = scale
        )
    }

    private companion object {
        const val SCALE_KEY = "app.ui_scale"
    }
}
