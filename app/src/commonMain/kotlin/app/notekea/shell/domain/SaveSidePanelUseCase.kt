package app.notekea.shell.domain

import app.notekea.core.notes.domain.SidePanelPreferences
import app.notekea.editor.domain.EditorPreferencesStore

/** Remembers how the panel on the right of the editor was left, its width kept within its limits. */
class SaveSidePanelUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(preferences: SidePanelPreferences) =
        editorPreferencesStore.setSidePanel(
            preferences = preferences.copy(
                widthDp = SidePanelPreferences.clampWidth(widthDp = preferences.widthDp)
            )
        )
}
