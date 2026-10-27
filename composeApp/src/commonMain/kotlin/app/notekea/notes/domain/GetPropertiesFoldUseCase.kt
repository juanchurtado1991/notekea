package app.notekea.notes.domain

/** Whether the properties above notes are shown open, as the user last left them; open on a first run. */
class GetPropertiesFoldUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(): Boolean = editorPreferencesStore.isPropertiesExpanded()
}
