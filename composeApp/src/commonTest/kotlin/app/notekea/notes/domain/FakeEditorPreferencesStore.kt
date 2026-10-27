package app.notekea.notes.domain

/** The editor's mode, side panel and properties fold in memory. */
class FakeEditorPreferencesStore : EditorPreferencesStore {
    var isReading = false
    var sidePanel = SidePanelPreferences.DEFAULT
    var isPropertiesExpanded = true

    override suspend fun isReadMode(): Boolean = isReading

    override suspend fun setReadMode(
        isReading: Boolean
    ) {
        this.isReading = isReading
    }

    override suspend fun sidePanel(): SidePanelPreferences = sidePanel

    override suspend fun setSidePanel(
        preferences: SidePanelPreferences
    ) {
        sidePanel = preferences
    }

    override suspend fun isPropertiesExpanded(): Boolean = isPropertiesExpanded

    override suspend fun setPropertiesExpanded(
        isExpanded: Boolean
    ) {
        isPropertiesExpanded = isExpanded
    }
}
