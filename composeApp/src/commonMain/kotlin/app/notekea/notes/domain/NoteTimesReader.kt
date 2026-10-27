package app.notekea.notes.domain

/** Reads when a note's file was created and last modified. */
interface NoteTimesReader {
    /** The note's [NoteTimes]; both null when the file cannot be read. */
    suspend fun times(
        vaultPath: String,
        path: RelativePath
    ): NoteTimes
}
