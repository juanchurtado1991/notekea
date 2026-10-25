package app.notekea.notes.domain

/** Remembers the mode the user chose, so the next note and the next run open in it. */
class SaveEditorModeUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(
        isReading: Boolean
    ) = editorPreferencesStore.setReadMode(isReading = isReading)
}
