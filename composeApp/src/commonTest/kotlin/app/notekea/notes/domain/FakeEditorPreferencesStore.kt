package app.notekea.notes.domain

/** The editor's mode and side panel in memory. */
class FakeEditorPreferencesStore : EditorPreferencesStore {
    var isReading = false
    var sidePanel = SidePanelPreferences.DEFAULT

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
}
