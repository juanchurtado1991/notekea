package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelTab
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [EditorPreferencesStore] on Multiplatform Settings, the same keys for all vaults: `editor.read_mode`,
 * `editor.side_panel_width`, `editor.side_panel_visible`, `editor.side_panel_tab` (its tab's name, Outline when the
 * name is unknown), `editor.properties_in_note` and `editor.properties_expanded`.
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
            isVisible = settings.getBoolean(key = SIDE_PANEL_VISIBLE_KEY, defaultValue = true),
            tab = settings.getStringOrNull(key = SIDE_PANEL_TAB_KEY)
                .let { name -> SidePanelTab.entries.firstOrNull { it.name == name } } ?: SidePanelTab.OUTLINE,
            arePropertiesInNote = settings.getBoolean(key = PROPERTIES_IN_NOTE_KEY, defaultValue = true)
        )
    }

    override suspend fun setSidePanel(
        preferences: SidePanelPreferences
    ) = withContext(context = dispatchers.io) {
        settings.putInt(key = SIDE_PANEL_WIDTH_KEY, value = preferences.widthDp)
        settings.putBoolean(key = SIDE_PANEL_VISIBLE_KEY, value = preferences.isVisible)
        settings.putString(key = SIDE_PANEL_TAB_KEY, value = preferences.tab.name)
        settings.putBoolean(key = PROPERTIES_IN_NOTE_KEY, value = preferences.arePropertiesInNote)
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
        private const val PROPERTIES_IN_NOTE_KEY = "editor.properties_in_note"
        private const val READ_MODE_KEY = "editor.read_mode"
        private const val SIDE_PANEL_TAB_KEY = "editor.side_panel_tab"
        private const val SIDE_PANEL_VISIBLE_KEY = "editor.side_panel_visible"
        private const val SIDE_PANEL_WIDTH_KEY = "editor.side_panel_width"
    }
}
