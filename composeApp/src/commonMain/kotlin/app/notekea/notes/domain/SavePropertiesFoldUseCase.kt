package app.notekea.notes.domain

/** Remembers whether the properties above notes are open, for every note and run. */
class SavePropertiesFoldUseCase(private val editorPreferencesStore: EditorPreferencesStore) {
    suspend operator fun invoke(
        isExpanded: Boolean
    ) = editorPreferencesStore.setPropertiesExpanded(isExpanded = isExpanded)
}
