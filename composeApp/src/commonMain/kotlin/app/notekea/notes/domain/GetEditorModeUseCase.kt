package app.notekea.notes.domain

/** Whether notes open in Read mode: the mode the user last chose, Edit mode on a first run. */
class GetEditorModeUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(): Boolean = editorPreferencesStore.isReadMode()
}
