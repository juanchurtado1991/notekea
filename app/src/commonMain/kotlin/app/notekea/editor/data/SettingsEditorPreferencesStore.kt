package app.notekea.editor.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.StringPatterns
import app.notekea.core.notes.domain.ConnectionsTab
import app.notekea.core.notes.domain.SidePanelMode
import app.notekea.core.notes.domain.SidePanelPreferences
import app.notekea.core.notes.domain.SidePanelSection
import app.notekea.editor.domain.EditorPreferencesStore
import com.russhwolf.settings.Settings
import kotlinx.coroutines.withContext

/**
 * [EditorPreferencesStore] on Multiplatform Settings, the same keys for all vaults, all under `editor.`: the read mode,
 * the side panel's width, whether it is visible, its mode (the inspector when the name is unknown) and its folded
 * sections (names of other versions left out), and whether the properties are expanded.
 */
class SettingsEditorPreferencesStore(
    private val settings: Settings,
    private val dispatchers: AppDispatchers
) : EditorPreferencesStore {
    override suspend fun isReadMode(): Boolean = withContext(context = dispatchers.io) {
        settings.getBoolean(
            key = READ_MODE_KEY,
            defaultValue = false
        )
    }

    override suspend fun setReadMode(isReading: Boolean) = withContext(context = dispatchers.io) {
        settings.putBoolean(
            key = READ_MODE_KEY,
            value = isReading
        )
    }

    override suspend fun sidePanel(): SidePanelPreferences = withContext(context = dispatchers.io) {
        SidePanelPreferences(
            widthDp = settings.getInt(
                key = SIDE_PANEL_WIDTH_KEY,
                defaultValue = SidePanelPreferences.DEFAULT_WIDTH_DP
            ),
            isVisible = settings.getBoolean(
                key = SIDE_PANEL_VISIBLE_KEY,
                defaultValue = true
            ),
            mode = settings.getStringOrNull(key = SIDE_PANEL_MODE_KEY)
                .let { name ->
                    SidePanelMode.entries.firstOrNull {
                        it.name == name
                    }
                } ?: SidePanelMode.INSPECTOR,
            foldedSections = settings
                .getString(
                    key = SIDE_PANEL_FOLDED_KEY,
                    defaultValue = StringPatterns.EMPTY
                )
                .split(SECTION_SEPARATOR)
                .mapNotNull { name ->
                    SidePanelSection.entries.firstOrNull {
                        it.name == name
                    }
                }
                .toSet(),
            connections = settings.getStringOrNull(key = SIDE_PANEL_CONNECTIONS_KEY)
                .let { name ->
                    ConnectionsTab.entries.firstOrNull {
                        it.name == name
                    }
                } ?: ConnectionsTab.RELATED
        )
    }

    override suspend fun setSidePanel(preferences: SidePanelPreferences) = withContext(
        context = dispatchers.io
    ) {
        settings.putInt(
            key = SIDE_PANEL_WIDTH_KEY,
            value = preferences.widthDp
        )

        settings.putBoolean(
            key = SIDE_PANEL_VISIBLE_KEY,
            value = preferences.isVisible
        )

        settings.putString(
            key = SIDE_PANEL_MODE_KEY,
            value = preferences.mode.name
        )

        settings.putString(
            key = SIDE_PANEL_CONNECTIONS_KEY,
            value = preferences.connections.name
        )

        settings.putString(
            key = SIDE_PANEL_FOLDED_KEY,
            value = preferences
                .foldedSections
                .joinToString(separator = SECTION_SEPARATOR) { it.name }
        )
    }

    override suspend fun isPropertiesExpanded(): Boolean = withContext(context = dispatchers.io) {
        settings.getBoolean(
            key = PROPERTIES_EXPANDED_KEY,
            defaultValue = true
        )
    }

    override suspend fun setPropertiesExpanded(isExpanded: Boolean) = withContext(
        context = dispatchers.io
    ) {
        settings.putBoolean(
            key = PROPERTIES_EXPANDED_KEY,
            value = isExpanded
        )
    }

    companion object {
        private const val PROPERTIES_EXPANDED_KEY = "editor.properties_expanded"
        private const val READ_MODE_KEY = "editor.read_mode"
        private const val SECTION_SEPARATOR = ","
        private const val SIDE_PANEL_CONNECTIONS_KEY = "editor.side_panel_connections"
        private const val SIDE_PANEL_FOLDED_KEY = "editor.side_panel_folded"
        private const val SIDE_PANEL_MODE_KEY = "editor.side_panel_mode"
        private const val SIDE_PANEL_VISIBLE_KEY = "editor.side_panel_visible"
        private const val SIDE_PANEL_WIDTH_KEY = "editor.side_panel_width"
    }
}
