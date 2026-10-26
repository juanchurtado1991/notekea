package app.notekea.notes.domain

/** Remembers how the panel on the right of the editor was left, its width kept within its limits. */
class SaveSidePanelUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(
        preferences: SidePanelPreferences
    ) = editorPreferencesStore.setSidePanel(
        preferences = preferences.copy(widthDp = SidePanelPreferences.clampWidth(widthDp = preferences.widthDp))
    )
}
