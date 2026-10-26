package app.notekea.notes.domain

/**
 * Keeps the editor's choices between notes and runs, for every vault: whether notes open in Read mode, and how the
 * panel on its right was left.
 */
interface EditorPreferencesStore {
    suspend fun isReadMode(): Boolean

    suspend fun setReadMode(
        isReading: Boolean
    )

    suspend fun sidePanel(): SidePanelPreferences

    suspend fun setSidePanel(
        preferences: SidePanelPreferences
    )
}
