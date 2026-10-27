package app.notekea.notes.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.StringPatterns
import app.notekea.notes.domain.EditorPreferencesStore
import app.notekea.notes.domain.SidePanelMode
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.SidePanelSection
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [EditorPreferencesStore] on Multiplatform Settings, the same keys for all vaults: `editor.read_mode`,
 * `editor.side_panel_width`, `editor.side_panel_visible`, `editor.side_panel_mode` (its mode's name, the inspector
 * when it is unknown), `editor.side_panel_folded` (the folded sections' names, those
 * of other versions left out) and `editor.properties_expanded`.
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
            mode = settings.getStringOrNull(key = SIDE_PANEL_MODE_KEY)
                .let { name -> SidePanelMode.entries.firstOrNull { it.name == name } } ?: SidePanelMode.INSPECTOR,
            foldedSections = settings.getString(key = SIDE_PANEL_FOLDED_KEY, defaultValue = StringPatterns.EMPTY)
                .split(SECTION_SEPARATOR)
                .mapNotNull { name -> SidePanelSection.entries.firstOrNull { it.name == name } }
                .toSet()
        )
    }

    override suspend fun setSidePanel(
        preferences: SidePanelPreferences
    ) = withContext(context = dispatchers.io) {
        settings.putInt(key = SIDE_PANEL_WIDTH_KEY, value = preferences.widthDp)
        settings.putBoolean(key = SIDE_PANEL_VISIBLE_KEY, value = preferences.isVisible)
        settings.putString(key = SIDE_PANEL_MODE_KEY, value = preferences.mode.name)
        settings.putString(
            key = SIDE_PANEL_FOLDED_KEY,
            value = preferences.foldedSections.joinToString(separator = SECTION_SEPARATOR) { it.name }
        )
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
        private const val SECTION_SEPARATOR = ","
        private const val SIDE_PANEL_FOLDED_KEY = "editor.side_panel_folded"
        private const val SIDE_PANEL_MODE_KEY = "editor.side_panel_mode"
        private const val SIDE_PANEL_VISIBLE_KEY = "editor.side_panel_visible"
        private const val SIDE_PANEL_WIDTH_KEY = "editor.side_panel_width"
    }
}
