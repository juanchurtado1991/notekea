package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.notes.domain.SidePanelPreferences
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [EditorPreferencesStore] on Multiplatform Settings, the same keys for all vaults: `editor.read_mode`,
 * `editor.side_panel_width` and `editor.side_panel_visible`.
 */
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

    override suspend fun sidePanel(): SidePanelPreferences = withContext(context = dispatchers.io) {
        SidePanelPreferences(
            widthDp = settings.getInt(key = SIDE_PANEL_WIDTH_KEY, defaultValue = SidePanelPreferences.DEFAULT_WIDTH_DP),
            isVisible = settings.getBoolean(key = SIDE_PANEL_VISIBLE_KEY, defaultValue = true)
        )
    }

    override suspend fun setSidePanel(
        preferences: SidePanelPreferences
    ) = withContext(context = dispatchers.io) {
        settings.putInt(key = SIDE_PANEL_WIDTH_KEY, value = preferences.widthDp)
        settings.putBoolean(key = SIDE_PANEL_VISIBLE_KEY, value = preferences.isVisible)
    }

    override suspend fun isPropertiesExpanded(): Boolean = withContext(context = dispatchers.io) {
        settings.getBoolean(key = PROPERTIES_EXPANDED_KEY, defaultValue = true)
    }

    override suspend fun setPropertiesExpanded(
        isExpanded: Boolean
    ) = withContext(context = dispatchers.io) {
        settings.putBoolean(key = PROPERTIES_EXPANDED_KEY, value = isExpanded)
    }

    companion object {
        private const val PROPERTIES_EXPANDED_KEY = "editor.properties_expanded"
        private const val READ_MODE_KEY = "editor.read_mode"
        private const val SIDE_PANEL_VISIBLE_KEY = "editor.side_panel_visible"
        private const val SIDE_PANEL_WIDTH_KEY = "editor.side_panel_width"
    }
}
