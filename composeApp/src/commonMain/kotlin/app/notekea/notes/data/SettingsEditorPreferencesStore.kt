package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.EditorPreferencesStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/** [EditorPreferencesStore] on Multiplatform Settings: one key for all vaults, `editor.read_mode`. */
class SettingsEditorPreferencesStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : EditorPreferencesStore {
    override suspend fun isReadMode(): Boolean = withContext(context = dispatchers.io) {
        settings.getBoolean(key = READ_MODE_KEY, defaultValue = false)
    }

    override suspend fun setReadMode(
        isReading: Boolean
    ) = withContext(context = dispatchers.io) {
        settings.putBoolean(key = READ_MODE_KEY, value = isReading)
    }

    companion object {
        private const val READ_MODE_KEY = "editor.read_mode"
    }
}
