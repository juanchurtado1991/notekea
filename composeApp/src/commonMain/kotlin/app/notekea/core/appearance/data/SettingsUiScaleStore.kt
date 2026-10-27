package app.notekea.core.appearance.data

import app.notekea.core.appearance.domain.UiScale
import app.notekea.core.appearance.domain.UiScaleStore
import app.notekea.core.domain.AppDispatchers
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [UiScaleStore] on Multiplatform Settings, the same for every vault: `app.ui_scale`. */
class SettingsUiScaleStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : UiScaleStore {
    override suspend fun scale(): Float = withContext(context = dispatchers.io) {
        settings.getFloat(key = SCALE_KEY, defaultValue = UiScale.DEFAULT)
    }

    override suspend fun setScale(
        scale: Float
    ) = withContext(context = dispatchers.io) {
        settings.putFloat(key = SCALE_KEY, value = scale)
    }

    private companion object {
        const val SCALE_KEY = "app.ui_scale"
    }
}
