package app.notekea.notes.domain

/** How the panel on the right of the editor was left, with its width kept within its limits; shown on a first run. */
class GetSidePanelUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(): SidePanelPreferences = editorPreferencesStore.sidePanel().let {
        it.copy(widthDp = SidePanelPreferences.clampWidth(widthDp = it.widthDp))
    }
}
