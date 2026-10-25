package app.notekea.notes.domain

/** The editor's mode in memory. */
class FakeEditorPreferencesStore : EditorPreferencesStore {
    var isReading = false

    override suspend fun isReadMode(): Boolean = isReading

    override suspend fun setReadMode(
        isReading: Boolean
    ) {
        this.isReading = isReading
    }
}
