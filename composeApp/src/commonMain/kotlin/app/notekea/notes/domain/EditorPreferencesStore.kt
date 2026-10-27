package app.notekea.notes.domain

/**
 * Keeps the editor's choices between notes and runs, for every vault: whether notes open in Read mode, how the panel
 * on its right was left, and whether the properties above notes are folded (`ROADMAP.md` 1.14).
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

    suspend fun isPropertiesExpanded(): Boolean

    suspend fun setPropertiesExpanded(
        isExpanded: Boolean
    )
}
