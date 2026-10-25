package app.notekea.notes.domain

/** Keeps the editor's mode between notes and runs: whether notes open in Read mode, for every vault. */
interface EditorPreferencesStore {
    suspend fun isReadMode(): Boolean

    suspend fun setReadMode(
        isReading: Boolean
    )
}
